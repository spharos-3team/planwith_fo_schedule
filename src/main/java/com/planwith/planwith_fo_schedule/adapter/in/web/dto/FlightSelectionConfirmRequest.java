package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.planwith.planwith_fo_schedule.domain.TripType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record FlightSelectionConfirmRequest(
		TripType tripType,
		@NotNull @Valid FlightCandidateRequest outboundCandidate,
		@Valid FlightCandidateRequest returnCandidate,
		Boolean refreshLatestInformation
) {
	public FlightSelectionConfirmRequest {
		tripType = tripType == null ? TripType.ONE_WAY : tripType;
		refreshLatestInformation = refreshLatestInformation == null ? Boolean.TRUE : refreshLatestInformation;
	}

	@JsonIgnore
	@AssertTrue(message = "returnCandidate is required for a round trip")
	public boolean isRoundTripReturnCandidatePresent() {
		return tripType != TripType.ROUND_TRIP || returnCandidate != null;
	}

	public record FlightCandidateRequest(
			@NotNull LocalDate flightDate,
			String flightStatus,
			@NotNull @Valid AirportScheduleRequest departure,
			@NotNull @Valid AirportScheduleRequest arrival,
			@NotBlank @Pattern(regexp = "(?i)[A-Z0-9]{2,3}") String carrierCode,
			@NotBlank @Pattern(regexp = "[A-Za-z0-9]{1,10}") String flightNumber,
			@Pattern(regexp = "(?i)[A-Z0-9]{2,3}") String operatingCarrierCode,
			String aircraftCode,
			@PositiveOrZero Long durationMinutes
	) {
	}

	public record AirportScheduleRequest(
			@NotBlank @Pattern(regexp = "(?i)[A-Z]{3}") String airportCode,
			String terminal,
			String gate,
			@NotNull OffsetDateTime scheduledAt,
			String timezone
	) {
	}
}
