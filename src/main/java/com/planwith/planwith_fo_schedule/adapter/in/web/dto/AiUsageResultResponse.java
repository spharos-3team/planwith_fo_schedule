package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_schedule.application.model.AiOperationType;
import com.planwith.planwith_fo_schedule.application.model.AiUsageResult;

public record AiUsageResultResponse(
		UUID memberUuid,
		UUID requestId,
		AiOperationType operationType,
		String model,
		long inputTokens,
		long outputTokens,
		long totalTokens
) {

	public static AiUsageResultResponse from(AiUsageResult usage) {
		if (usage == null) {
			return null;
		}
		return new AiUsageResultResponse(
				usage.memberUuid(),
				usage.requestId(),
				usage.operationType(),
				usage.model(),
				usage.inputTokens(),
				usage.outputTokens(),
				usage.totalTokens()
		);
	}
}
