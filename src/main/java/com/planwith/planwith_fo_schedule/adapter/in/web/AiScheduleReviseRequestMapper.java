package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleReviseRequest;
import com.planwith.planwith_fo_schedule.application.exception.AuthenticationRequiredException;
import com.planwith.planwith_fo_schedule.application.port.in.ReviseScheduleWithAiUseCase.ReviseScheduleCommand;

public final class AiScheduleReviseRequestMapper {

	private AiScheduleReviseRequestMapper() {
	}

	public static ReviseScheduleCommand toCommand(
			UUID authenticatedMemberUuid,
			UUID scheduleUuid,
			AiScheduleReviseRequest request
	) {
		if (authenticatedMemberUuid == null) {
			throw new AuthenticationRequiredException();
		}
		AiScheduleReviseRequest validatedRequest = Objects.requireNonNull(
				request,
				"AI schedule revision request is required."
		);
		return new ReviseScheduleCommand(
				authenticatedMemberUuid,
				Objects.requireNonNull(scheduleUuid, "Schedule UUID is required."),
				validatedRequest.additionalRequest()
		);
	}
}
