package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleFlightRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleGenerateRequest;
import com.planwith.planwith_fo_schedule.application.command.AiScheduleGenerateCommand;
import com.planwith.planwith_fo_schedule.application.command.AiScheduleGenerateCommand.AiScheduleFlightCommand;
import com.planwith.planwith_fo_schedule.application.exception.AuthenticationRequiredException;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;

public final class AiScheduleGenerateRequestMapper {

	private AiScheduleGenerateRequestMapper() {
	}

	public static AiScheduleGenerateCommand toCommand(
			UUID authenticatedMemberUuid,
			AiScheduleGenerateRequest request
	) {
		if (authenticatedMemberUuid == null) {
			throw new AuthenticationRequiredException();
		}
		AiScheduleGenerateRequest validatedRequest = Objects.requireNonNull(request, "AI schedule request is required.");

		return new AiScheduleGenerateCommand(
				new MemberUuid(authenticatedMemberUuid),
				validatedRequest.destination(),
				new SchedulePeriod(validatedRequest.startDate(), validatedRequest.endDate()),
				new Headcount(validatedRequest.participantCount()),
				ScheduleCost.of(validatedRequest.estimatedBudget()),
				validatedRequest.transportation(),
				validatedRequest.additionalRequest(),
				toFlightCommand(validatedRequest.flight())
		);
	}

	private static AiScheduleFlightCommand toFlightCommand(AiScheduleFlightRequest flight) {
		if (flight == null) {
			return null;
		}
		return new AiScheduleFlightCommand(
				flight.departureLocation(),
				flight.originLocationCode(),
				flight.destinationLocationCode(),
				flight.tripType(),
				flight.travelClass()
		);
	}
}
