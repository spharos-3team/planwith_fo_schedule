package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.UUID;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSelectionConfirmRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSelectionConfirmRequest.AirportScheduleRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSelectionConfirmRequest.FlightCandidateRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSelectionConfirmResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSelectionConfirmResponse.ConfirmedFlightResponse;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate.AirportSchedule;
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
				toCandidate(request.outboundCandidate()),
				toCandidate(request.returnCandidate()),
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

	private static FlightCandidate toCandidate(FlightCandidateRequest candidate) {
		if (candidate == null) {
			return null;
		}
		return new FlightCandidate(
				candidate.flightDate(), candidate.flightStatus(),
				toAirportSchedule(candidate.departure()), toAirportSchedule(candidate.arrival()),
				candidate.carrierCode(), candidate.flightNumber(), candidate.operatingCarrierCode(),
				candidate.aircraftCode(), candidate.durationMinutes()
		);
	}

	private static AirportSchedule toAirportSchedule(AirportScheduleRequest schedule) {
		return schedule == null ? null : new AirportSchedule(
				schedule.airportCode(), schedule.terminal(), schedule.gate(), schedule.scheduledAt(), schedule.timezone()
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
