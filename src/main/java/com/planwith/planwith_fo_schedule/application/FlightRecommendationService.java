package com.planwith.planwith_fo_schedule.application;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.model.FlightRecommendation;
import com.planwith.planwith_fo_schedule.application.model.FlightRecommendationCacheKey;
import com.planwith.planwith_fo_schedule.application.port.in.RecommendFlightsUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase.FlightSearchCommand;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase.FlightSearchResult;
import com.planwith.planwith_fo_schedule.application.port.out.FlightRecommendationCachePort;
import com.planwith.planwith_fo_schedule.domain.TripType;

@Service
public class FlightRecommendationService implements RecommendFlightsUseCase {

	private static final Logger log = LoggerFactory.getLogger(FlightRecommendationService.class);

	private final SearchFlightsUseCase searchFlightsUseCase;
	private final FlightRecommendationPolicy recommendationPolicy;
	private final FlightRecommendationCachePort cachePort;

	public FlightRecommendationService(
			SearchFlightsUseCase searchFlightsUseCase,
			FlightRecommendationPolicy recommendationPolicy,
			FlightRecommendationCachePort cachePort
	) {
		this.searchFlightsUseCase = searchFlightsUseCase;
		this.recommendationPolicy = recommendationPolicy;
		this.cachePort = cachePort;
	}

	@Override
	public FlightRecommendation recommend(FlightRecommendationCommand command) {
		FlightRecommendationCacheKey cacheKey = toCacheKey(command);
		Optional<FlightRecommendation> cachedRecommendation = cachePort.find(cacheKey);
		if (cachedRecommendation.isPresent()) {
			log.info("FlightRecommendationService : recommend : 항공편 추천 캐시 조회 성공 - tripType={}",
					cacheKey.tripType());
			return cachedRecommendation.get();
		}

		log.info("FlightRecommendationService : recommend : 항공편 추천 생성 시작 - tripType={}",
				cacheKey.tripType());
		FlightSearchResult searchResult = searchFlightsUseCase.search(toSearchCommand(command));
		List<FlightCandidate> outboundRecommendations = recommendationPolicy.recommend(
				searchResult.outboundCandidates(),
				cacheKey.departureAirportCode(),
				cacheKey.arrivalAirportCode()
		);
		List<FlightCandidate> returnRecommendations = recommendationPolicy.recommend(
				searchResult.returnCandidates(),
				cacheKey.arrivalAirportCode(),
				cacheKey.departureAirportCode()
		);
		FlightRecommendation recommendation = new FlightRecommendation(
				searchResult.tripType(), outboundRecommendations, returnRecommendations
		);
		cachePort.save(cacheKey, recommendation);
		log.info("FlightRecommendationService : recommend : 항공편 추천 생성 완료 - outboundCount={}, returnCount={}",
				outboundRecommendations.size(), returnRecommendations.size());
		return recommendation;
	}

	private FlightSearchCommand toSearchCommand(FlightRecommendationCommand command) {
		return new FlightSearchCommand(
				command.departureAirportCode(),
				command.arrivalAirportCode(),
				command.departureDate(),
				command.returnDate(),
				command.tripType()
		);
	}

	private FlightRecommendationCacheKey toCacheKey(FlightRecommendationCommand command) {
		TripType tripType = command.tripType() == null ? TripType.ROUND_TRIP : command.tripType();
		return new FlightRecommendationCacheKey(
				normalize(command.departureAirportCode()),
				normalize(command.arrivalAirportCode()),
				command.departureDate(),
				command.returnDate(),
				tripType
		);
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
	}
}
