package com.planwith.planwith_fo_schedule.application.port.in;

import java.util.UUID;

public interface ReviseScheduleWithAiUseCase {

	ReviseScheduleResult revise(ReviseScheduleCommand command);

	record ReviseScheduleCommand(
			UUID authenticatedMemberUuid,
			UUID scheduleUuid,
			String additionalRequest
	) {
	}

	record ReviseScheduleResult(
			UUID scheduleUuid,
			String revisedTitle,
			String revisedContent
	) {
	}
}
