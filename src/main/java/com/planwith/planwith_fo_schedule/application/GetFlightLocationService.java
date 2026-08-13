package com.planwith.planwith_fo_schedule.application;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_schedule.application.exception.AirportCodeNotFoundException;
import com.planwith.planwith_fo_schedule.application.exception.FlightLocationNotSupportedException;
import com.planwith.planwith_fo_schedule.application.exception.InvalidFlightLocationException;
import com.planwith.planwith_fo_schedule.application.port.in.GetFlightLocationUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.FlightLocationPort;

@Service
public class GetFlightLocationService implements GetFlightLocationUseCase {

	private static final Logger log = LoggerFactory.getLogger(GetFlightLocationService.class);
	private static final String IATA_CODE_PATTERN = "[A-Z]{3}";

	private final FlightLocationPort flightLocationPort;

	public GetFlightLocationService(FlightLocationPort flightLocationPort) {
		this.flightLocationPort = flightLocationPort;
	}

	@Override
	public FlightLocationResult getAirportCodes(String location) {
		String normalizedLocation = normalize(location);
		log.debug("GetFlightLocationService : getAirportCodes : 지역별 공항 코드 조회 시작 - location={}",
				normalizedLocation);

		List<String> airportCodes = flightLocationPort.findAirportCodes(normalizedLocation)
				.orElseThrow(() -> new FlightLocationNotSupportedException(normalizedLocation));
		List<String> validatedAirportCodes = airportCodes.stream()
				.map(code -> code == null ? null : code.trim().toUpperCase(Locale.ROOT))
				.filter(code -> code != null && code.matches(IATA_CODE_PATTERN))
				.distinct()
				.toList();
		if (validatedAirportCodes.isEmpty()) {
			throw new AirportCodeNotFoundException(normalizedLocation);
		}

		log.debug("GetFlightLocationService : getAirportCodes : 지역별 공항 코드 조회 완료 - location={}, airportCount={}",
				normalizedLocation, validatedAirportCodes.size());
		return new FlightLocationResult(normalizedLocation, validatedAirportCodes);
	}

	private String normalize(String location) {
		if (location == null || location.isBlank()) {
			throw new InvalidFlightLocationException();
		}
		return location.trim().toLowerCase(Locale.ROOT);
	}
}
