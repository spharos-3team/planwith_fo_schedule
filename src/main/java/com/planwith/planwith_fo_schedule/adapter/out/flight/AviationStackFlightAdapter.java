package com.planwith.planwith_fo_schedule.adapter.out.flight;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.planwith.planwith_fo_schedule.application.exception.FlightSearchException;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.port.out.FlightSearchPort;
import com.planwith.planwith_fo_schedule.config.AviationStackProperties;

@Component
public class AviationStackFlightAdapter implements FlightSearchPort {

	private static final Logger log = LoggerFactory.getLogger(AviationStackFlightAdapter.class);

	private final RestClient aviationStackRestClient;
	private final AviationStackProperties properties;
	private final AviationStackFlightMapper flightMapper;

	public AviationStackFlightAdapter(
			@Qualifier("aviationStackRestClient") RestClient aviationStackRestClient,
			AviationStackProperties properties,
			AviationStackFlightMapper flightMapper
	) {
		this.aviationStackRestClient = aviationStackRestClient;
		this.properties = properties;
		this.flightMapper = flightMapper;
	}

	@Override
	public List<FlightCandidate> search(FlightSearchCriteria criteria) {
		validateConfiguration();
		log.info("AviationStackFlightAdapter : search : AviationStack 항공편 조회 시작 - departure={}, arrival={}, flightDate={}",
				criteria.departureAirportCode(), criteria.arrivalAirportCode(), criteria.flightDate());
		try {
			AviationStackFlightsResponse response = aviationStackRestClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/flights")
							.queryParam("access_key", properties.getAccessKey())
							.queryParam("dep_iata", criteria.departureAirportCode())
							.queryParam("arr_iata", criteria.arrivalAirportCode())
							.queryParamIfPresent("flight_number", optionalText(criteria.flightNumber()))
							.queryParam("limit", 100)
							.build())
					.retrieve()
					.body(AviationStackFlightsResponse.class);
			if (response == null) {
				throw new FlightSearchException("AviationStack returned an empty response.");
			}
			if (response.error() != null) {
				log.warn("AviationStackFlightAdapter : search : AviationStack 항공편 조회 거부 - code={}",
						response.error().code());
				throw new FlightSearchException("AviationStack rejected the flight search request.");
			}
			List<FlightCandidate> candidates = response.data() == null
					? List.of()
					: response.data().stream()
							.filter(flight -> criteria.flightDate().equals(parseDate(flight.flightDate())))
							.map(flightMapper::toCandidate)
							.toList();
			log.info("AviationStackFlightAdapter : search : AviationStack 항공편 조회 완료 - candidateCount={}",
					candidates.size());
			return candidates;
		} catch (RestClientResponseException exception) {
			String providerCode = extractProviderCode(exception);
			log.warn("AviationStackFlightAdapter : search : AviationStack HTTP 오류 - status={}, providerCode={}",
					exception.getStatusCode().value(), providerCode);
			throw new FlightSearchException(
					"AviationStack rejected the flight search request.",
					providerCode,
					exception
			);
		} catch (RestClientException exception) {
			log.warn("AviationStackFlightAdapter : search : AviationStack 통신 오류 - exceptionType={}",
					exception.getClass().getSimpleName());
			throw new FlightSearchException("Failed to communicate with AviationStack.", exception);
		}
	}

	private Optional<String> optionalText(String value) {
		return value == null || value.isBlank() ? Optional.empty() : Optional.of(value.trim());
	}

	private String extractProviderCode(RestClientResponseException exception) {
		try {
			AviationStackFlightsResponse response = exception.getResponseBodyAs(AviationStackFlightsResponse.class);
			return response == null || response.error() == null
					? "unknown"
					: String.valueOf(response.error().code());
		} catch (RuntimeException parsingException) {
			return "unknown";
		}
	}

	private LocalDate parseDate(String value) {
		try {
			return value == null ? null : LocalDate.parse(value);
		} catch (DateTimeParseException exception) {
			return null;
		}
	}

	private void validateConfiguration() {
		if (!properties.isEnabled()) {
			throw new FlightSearchException("AviationStack integration is disabled.");
		}
		if (properties.getAccessKey() == null || properties.getAccessKey().isBlank()) {
			throw new FlightSearchException("AVIATIONSTACK_ACCESS_KEY is not configured.");
		}
	}
}
