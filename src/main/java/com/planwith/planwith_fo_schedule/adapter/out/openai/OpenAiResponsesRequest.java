package com.planwith.planwith_fo_schedule.adapter.out.openai;

import java.util.Map;

record OpenAiResponsesRequest(
		String model,
		String instructions,
		String input,
		TextConfiguration text
) {
	record TextConfiguration(StructuredFormat format) {
	}

	record StructuredFormat(
		String type,
		String name,
		boolean strict,
		Map<String, Object> schema
	) {
	}
}
