package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleSaveRequest;
import com.planwith.planwith_fo_schedule.application.exception.AuthenticationRequiredException;
import com.planwith.planwith_fo_schedule.application.port.in.SaveAiScheduleUseCase.SaveAiScheduleCommand;
import com.planwith.planwith_fo_schedule.application.port.in.SaveAiScheduleUseCase.SaveAiScheduleItemCommand;
import com.planwith.planwith_fo_schedule.application.port.in.SaveAiScheduleUseCase.SaveAiScheduleFlightCommand;

final class AiScheduleSaveRequestMapper {

	private AiScheduleSaveRequestMapper() {
	}

	static SaveAiScheduleCommand toCommand(UUID authenticatedMemberUuid, AiScheduleSaveRequest request) {
		if (authenticatedMemberUuid == null) {
			throw new AuthenticationRequiredException();
		}
		AiScheduleSaveRequest validatedRequest = Objects.requireNonNull(
				request,
				"AI schedule save request is required."
		);
		return new SaveAiScheduleCommand(
				authenticatedMemberUuid,
				validatedRequest.title(),
				validatedRequest.destination(),
				validatedRequest.imageUrl(),
				validatedRequest.startDate(),
				validatedRequest.endDate(),
				validatedRequest.participantCount(),
				validatedRequest.estimatedBudget(),
				validatedRequest.transportation(),
				validatedRequest.travelStyle(),
				validatedRequest.content(),
				validatedRequest.calendarColor(),
				validatedRequest.items().stream().map(AiScheduleSaveRequestMapper::toItemCommand).toList(),
				toFlightCommand(validatedRequest.flight())
		);
	}

	private static SaveAiScheduleFlightCommand toFlightCommand(
			AiScheduleSaveRequest.SelectedFlightSaveRequest flight
	) {
		if (flight == null) {
			return null;
		}
		return new SaveAiScheduleFlightCommand(
				flight.departureLocation(),
				flight.tripType(),
				flight.outboundCandidates().stream().map(FlightCandidateRequestMapper::toCandidate).toList(),
				flight.returnCandidates().stream().map(FlightCandidateRequestMapper::toCandidate).toList()
		);
	}

	private static SaveAiScheduleItemCommand toItemCommand(
			AiScheduleSaveRequest.AiScheduleItemSaveRequest item
	) {
		return new SaveAiScheduleItemCommand(
				item.dayNumber(),
				item.scheduleTime(),
				item.subtitle(),
				item.scheduleType(),
				item.description(),
				item.estimatedCost(),
				item.placeName(),
				item.placeAddress(),
				item.latitude(),
				item.longitude()
		);
	}
}
