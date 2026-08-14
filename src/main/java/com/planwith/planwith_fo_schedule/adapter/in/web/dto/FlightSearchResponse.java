package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.planwith.planwith_fo_schedule.domain.TripType;

public record FlightSearchResponse(
		TripType tripType,
		List<FlightCandidateResponse> outboundCandidates,
		List<FlightCandidateResponse> returnCandidates
) {
	public FlightSearchResponse {
		outboundCandidates = List.copyOf(outboundCandidates);
		returnCandidates = List.copyOf(returnCandidates);
	}

	public record FlightCandidateResponse(
			LocalDate flightDate,
			String flightStatus,
			AirportScheduleResponse departure,
			AirportScheduleResponse arrival,
			String carrierCode,
			String flightNumber,
			String operatingCarrierCode,
			String aircraftCode,
			Long durationMinutes
	) {
	}

	public record AirportScheduleResponse(
			String airportCode,
			String terminal,
			String gate,
			OffsetDateTime scheduledAt,
			String timezone
	) {
	}
}
