package com.planwith.planwith_fo_schedule.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_schedule.application.exception.InvalidFlightSearchException;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.FlightSearchPort;
import com.planwith.planwith_fo_schedule.application.port.out.FlightSearchPort.FlightSearchCriteria;
import com.planwith.planwith_fo_schedule.domain.FlightTripType;

@Service
public class FlightSearchService implements SearchFlightsUseCase {

	private static final Logger log = LoggerFactory.getLogger(FlightSearchService.class);
	private static final String IATA_CODE_PATTERN = "[A-Z]{3}";

	private final FlightSearchPort flightSearchPort;

	public FlightSearchService(FlightSearchPort flightSearchPort) {
		this.flightSearchPort = flightSearchPort;
	}

	@Override
	public FlightSearchResult search(FlightSearchCommand command) {
		FlightSearchCommand validatedCommand = validate(command);
		log.info("FlightSearchService : search : 항공편 후보 검색 시작 - tripType={}",
				validatedCommand.tripType());

		List<FlightCandidate> outboundCandidates = flightSearchPort.search(new FlightSearchCriteria(
				validatedCommand.departureAirportCode(),
				validatedCommand.arrivalAirportCode(),
				validatedCommand.departureDate()
		));
		List<FlightCandidate> returnCandidates = validatedCommand.tripType() == FlightTripType.ROUND_TRIP
				? flightSearchPort.search(new FlightSearchCriteria(
						validatedCommand.arrivalAirportCode(),
						validatedCommand.departureAirportCode(),
						validatedCommand.returnDate()
				))
				: List.of();

		log.info("FlightSearchService : search : 항공편 후보 검색 완료 - outboundCount={}, returnCount={}",
				outboundCandidates.size(), returnCandidates.size());
		return new FlightSearchResult(validatedCommand.tripType(), outboundCandidates, returnCandidates);
	}

	private FlightSearchCommand validate(FlightSearchCommand command) {
		Objects.requireNonNull(command, "Flight search command is required.");
		String departureAirportCode = normalizeIataCode(command.departureAirportCode(), "Departure airport code");
		String arrivalAirportCode = normalizeIataCode(command.arrivalAirportCode(), "Arrival airport code");
		if (departureAirportCode.equals(arrivalAirportCode)) {
			throw new InvalidFlightSearchException("Departure and arrival airport codes must be different.");
		}
		LocalDate departureDate = Objects.requireNonNull(command.departureDate(), "Departure date is required.");
		FlightTripType tripType = command.tripType() == null ? FlightTripType.ROUND_TRIP : command.tripType();
		LocalDate returnDate = command.returnDate();
		if (tripType == FlightTripType.ROUND_TRIP && returnDate == null) {
			throw new InvalidFlightSearchException("Return date is required for a round trip.");
		}
		if (returnDate != null && returnDate.isBefore(departureDate)) {
			throw new InvalidFlightSearchException("Return date must not be before departure date.");
		}
		return new FlightSearchCommand(
				departureAirportCode,
				arrivalAirportCode,
				departureDate,
				returnDate,
				tripType
		);
	}

	private String normalizeIataCode(String code, String fieldName) {
		if (code == null || code.isBlank()) {
			throw new InvalidFlightSearchException(fieldName + " is required.");
		}
		String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
		if (!normalizedCode.matches(IATA_CODE_PATTERN)) {
			throw new InvalidFlightSearchException(fieldName + " must be a three-letter IATA code.");
		}
		return normalizedCode;
	}
}
