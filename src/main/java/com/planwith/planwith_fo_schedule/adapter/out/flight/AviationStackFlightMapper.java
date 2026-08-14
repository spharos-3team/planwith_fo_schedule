package com.planwith.planwith_fo_schedule.adapter.out.flight;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate.AirportSchedule;

@Component
class AviationStackFlightMapper {

	FlightCandidate toCandidate(AviationStackFlightsResponse.FlightData flight) {
		AirportSchedule departure = toAirportSchedule(flight.departure());
		AirportSchedule arrival = toAirportSchedule(flight.arrival());
		return new FlightCandidate(
				parseDate(flight.flightDate()),
				flight.flightStatus(),
				departure,
				arrival,
				flight.airline() == null ? null : flight.airline().iata(),
				flight.flight() == null ? null : flight.flight().number(),
				operatingCarrierCode(flight.flight()),
				flight.aircraft() == null ? null : flight.aircraft().iata(),
				durationMinutes(departure.scheduledAt(), arrival.scheduledAt())
		);
	}

	private AirportSchedule toAirportSchedule(AviationStackFlightsResponse.AirportEndpoint endpoint) {
		if (endpoint == null) {
			return new AirportSchedule(null, null, null, null, null);
		}
		return new AirportSchedule(
				endpoint.iata(),
				endpoint.terminal(),
				endpoint.gate(),
				parseDateTime(endpoint.scheduled()),
				endpoint.timezone()
		);
	}

	private String operatingCarrierCode(AviationStackFlightsResponse.Flight flight) {
		return flight == null || flight.codeshared() == null ? null : flight.codeshared().airlineIata();
	}

	private Long durationMinutes(OffsetDateTime departureAt, OffsetDateTime arrivalAt) {
		if (departureAt == null || arrivalAt == null || !arrivalAt.isAfter(departureAt)) {
			return null;
		}
		return Duration.between(departureAt, arrivalAt).toMinutes();
	}

	private LocalDate parseDate(String value) {
		try {
			return value == null ? null : LocalDate.parse(value);
		} catch (DateTimeParseException exception) {
			return null;
		}
	}

	private OffsetDateTime parseDateTime(String value) {
		try {
			return value == null ? null : OffsetDateTime.parse(value);
		} catch (DateTimeParseException exception) {
			return null;
		}
	}
}
