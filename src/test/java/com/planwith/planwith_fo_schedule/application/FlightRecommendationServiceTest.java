package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.model.FlightRecommendation;
import com.planwith.planwith_fo_schedule.application.model.FlightRecommendationCacheKey;
import com.planwith.planwith_fo_schedule.application.port.in.RecommendFlightsUseCase.FlightRecommendationCommand;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase.FlightSearchResult;
import com.planwith.planwith_fo_schedule.application.port.out.FlightRecommendationCachePort;
import com.planwith.planwith_fo_schedule.domain.TripType;

class FlightRecommendationServiceTest {

	private SearchFlightsUseCase searchFlightsUseCase;
	private FlightRecommendationCachePort cachePort;
	private FlightRecommendationService service;

	@BeforeEach
	void setUp() {
		searchFlightsUseCase = mock(SearchFlightsUseCase.class);
		cachePort = mock(FlightRecommendationCachePort.class);
		service = new FlightRecommendationService(
				searchFlightsUseCase,
				new FlightRecommendationPolicy(),
				cachePort
		);
	}

	@Test
	void returnsCachedRecommendationWithoutCallingFlightApi() {
		FlightRecommendationCommand command = command();
		FlightRecommendation cached = new FlightRecommendation(
				TripType.ONE_WAY, List.of(candidate("100", 100)), List.of()
		);
		when(cachePort.find(any())).thenReturn(Optional.of(cached));

		assertThat(service.recommend(command)).isSameAs(cached);
		verify(searchFlightsUseCase, never()).search(any());
		verify(cachePort, never()).save(any(), any());
	}

	@Test
	void searchesRanksTopThreeAndCachesResultWhenCacheMisses() {
		when(cachePort.find(any())).thenReturn(Optional.empty());
		when(searchFlightsUseCase.search(any())).thenReturn(new FlightSearchResult(
				TripType.ONE_WAY,
				List.of(candidate("104", 140), candidate("101", 80), candidate("103", 120), candidate("102", 100)),
				List.of()
		));

		FlightRecommendation result = service.recommend(command());

		assertThat(result.outboundCandidates()).extracting(candidate -> candidate.flightNumber())
				.containsExactly("101", "102", "103");
		verify(searchFlightsUseCase).search(any());
		verify(cachePort).save(any(FlightRecommendationCacheKey.class), any(FlightRecommendation.class));
	}

	private FlightRecommendationCommand command() {
		return new FlightRecommendationCommand(
				"icn", "nrt", LocalDate.of(2026, 8, 13), null, TripType.ONE_WAY
		);
	}

	private FlightCandidate candidate(String flightNumber, long duration) {
		return new FlightCandidate(
				LocalDate.of(2026, 8, 13), "scheduled",
				new FlightCandidate.AirportSchedule("ICN", null, null, null, null),
				new FlightCandidate.AirportSchedule("NRT", null, null, null, null),
				"KE", flightNumber, null, null, duration
		);
	}
}
