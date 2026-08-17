package com.planwith.planwith_fo_schedule.application.model;

public record OpenAiUsage(
		String model,
		long inputTokens,
		long outputTokens,
		long totalTokens
) {

	public OpenAiUsage {
		if (model == null || model.isBlank()) {
			throw new IllegalArgumentException("OpenAI response model is required.");
		}
		if (inputTokens < 0 || outputTokens < 0 || totalTokens < 0) {
			throw new IllegalArgumentException("OpenAI token usage cannot be negative.");
		}
	}
}
