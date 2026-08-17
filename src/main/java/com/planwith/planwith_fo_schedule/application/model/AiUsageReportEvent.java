package com.planwith.planwith_fo_schedule.application.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AiUsageReportEvent(
		UUID memberUuid,
		UUID requestId,
		AiOperationType operationType,
		String model,
		long inputTokens,
		long outputTokens,
		long totalTokens,
		Instant occurredAt
) {

	public AiUsageReportEvent {
		Objects.requireNonNull(memberUuid, "Member UUID is required.");
		Objects.requireNonNull(requestId, "Request ID is required.");
		Objects.requireNonNull(operationType, "AI operation type is required.");
		Objects.requireNonNull(occurredAt, "Occurred time is required.");
		if (model == null || model.isBlank()) {
			throw new IllegalArgumentException("OpenAI response model is required.");
		}
		if (inputTokens < 0 || outputTokens < 0 || totalTokens < 0) {
			throw new IllegalArgumentException("Reported OpenAI token usage cannot be negative.");
		}
	}
}
