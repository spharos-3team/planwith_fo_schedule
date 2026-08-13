package com.planwith.planwith_fo_schedule.application.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record FlightCandidate(
		LocalDate flightDate,
		String flightStatus,
		AirportSchedule departure,
		AirportSchedule arrival,
		String carrierCode,
		String flightNumber,
		String operatingCarrierCode,
		String aircraftCode,
		Long durationMinutes
) {

	public record AirportSchedule(
			String airportCode,
			String terminal,
			String gate,
			OffsetDateTime scheduledAt,
			String timezone
	) {
	}
}
