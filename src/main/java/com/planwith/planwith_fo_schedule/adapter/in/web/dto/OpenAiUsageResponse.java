package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import com.planwith.planwith_fo_schedule.application.model.OpenAiUsage;

public record OpenAiUsageResponse(
		String model,
		long inputTokens,
		long outputTokens,
		long totalTokens
) {

	public static OpenAiUsageResponse from(OpenAiUsage usage) {
		if (usage == null) {
			return null;
		}
		return new OpenAiUsageResponse(
				usage.model(),
				usage.inputTokens(),
				usage.outputTokens(),
				usage.totalTokens()
		);
	}
}
