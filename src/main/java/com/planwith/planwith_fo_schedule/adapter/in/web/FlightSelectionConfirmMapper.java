package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.UUID;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSelectionConfirmRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSelectionConfirmResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSelectionConfirmResponse.ConfirmedFlightResponse;
import com.planwith.planwith_fo_schedule.application.exception.AuthenticationRequiredException;
import com.planwith.planwith_fo_schedule.application.port.in.ConfirmFlightSelectionUseCase.ConfirmFlightSelectionCommand;
import com.planwith.planwith_fo_schedule.application.port.in.ConfirmFlightSelectionUseCase.ConfirmedFlight;
import com.planwith.planwith_fo_schedule.application.port.in.ConfirmFlightSelectionUseCase.FlightSelectionConfirmation;

final class FlightSelectionConfirmMapper {

	private FlightSelectionConfirmMapper() {
	}

	static ConfirmFlightSelectionCommand toCommand(UUID memberUuid, FlightSelectionConfirmRequest request) {
		if (memberUuid == null) {
			throw new AuthenticationRequiredException();
		}
		return new ConfirmFlightSelectionCommand(
				memberUuid,
				request.tripType(),
				FlightCandidateRequestMapper.toCandidate(request.outboundCandidate()),
				FlightCandidateRequestMapper.toCandidate(request.returnCandidate()),
				Boolean.TRUE.equals(request.refreshLatestInformation())
		);
	}

	static FlightSelectionConfirmResponse toResponse(FlightSelectionConfirmation confirmation) {
		return new FlightSelectionConfirmResponse(
				confirmation.memberUuid(),
				confirmation.tripType(),
				toConfirmedFlightResponse(confirmation.outboundFlight()),
				toConfirmedFlightResponse(confirmation.returnFlight()),
				confirmation.confirmedAt()
		);
	}

	private static ConfirmedFlightResponse toConfirmedFlightResponse(ConfirmedFlight flight) {
		return flight == null ? null : new ConfirmedFlightResponse(
				FlightSearchResponseMapper.toCandidateResponse(flight.candidate()),
				flight.refreshed(),
				flight.informationChanged()
		);
	}
}
