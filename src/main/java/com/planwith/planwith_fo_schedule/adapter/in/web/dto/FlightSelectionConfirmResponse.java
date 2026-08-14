package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSearchResponse.FlightCandidateResponse;
import com.planwith.planwith_fo_schedule.domain.FlightTripType;

public record FlightSelectionConfirmResponse(
		UUID memberUuid,
		FlightTripType tripType,
		ConfirmedFlightResponse outboundFlight,
		ConfirmedFlightResponse returnFlight,
		OffsetDateTime confirmedAt
) {

	public record ConfirmedFlightResponse(
			FlightCandidateResponse candidate,
			boolean refreshed,
			boolean informationChanged
	) {
	}
}
