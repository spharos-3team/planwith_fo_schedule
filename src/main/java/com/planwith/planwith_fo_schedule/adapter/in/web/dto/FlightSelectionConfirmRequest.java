package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.planwith.planwith_fo_schedule.domain.TripType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

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
}
