package com.planwith.planwith_fo_schedule.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_schedule.application.model.AiUsageResult;

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
			String revisedContent,
			AiUsageResult usage
	) {
		public ReviseScheduleResult(UUID scheduleUuid, String revisedContent) {
			this(scheduleUuid, revisedContent, null);
		}
	}
}
