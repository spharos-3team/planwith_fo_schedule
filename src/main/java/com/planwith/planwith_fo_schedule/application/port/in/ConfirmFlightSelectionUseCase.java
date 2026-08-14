package com.planwith.planwith_fo_schedule.application.port.in;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.domain.FlightTripType;

public interface ConfirmFlightSelectionUseCase {

	FlightSelectionConfirmation confirm(ConfirmFlightSelectionCommand command);

	record ConfirmFlightSelectionCommand(
			UUID memberUuid,
			FlightTripType tripType,
			FlightCandidate outboundCandidate,
			FlightCandidate returnCandidate,
			boolean refreshLatestInformation
	) {
	}

	record FlightSelectionConfirmation(
			UUID memberUuid,
			FlightTripType tripType,
			ConfirmedFlight outboundFlight,
			ConfirmedFlight returnFlight,
			OffsetDateTime confirmedAt
	) {
	}

	record ConfirmedFlight(
			FlightCandidate candidate,
			boolean refreshed,
			boolean informationChanged
	) {
	}
}
