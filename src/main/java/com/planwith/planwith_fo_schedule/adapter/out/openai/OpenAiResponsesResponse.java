package com.planwith.planwith_fo_schedule.adapter.out.openai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
record OpenAiResponsesResponse(
		List<Output> output,
		String model,
		Usage usage
) {

	String outputText() {
		if (output == null) {
			return null;
		}
		return output.stream()
				.filter(item -> item.content() != null)
				.flatMap(item -> item.content().stream())
				.filter(content -> "output_text".equals(content.type()))
				.map(content -> content.text())
				.filter(text -> text != null && !text.isBlank())
				.findFirst()
				.orElse(null);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Output(List<Content> content) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Content(String type, String text, String refusal) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Usage(
			@JsonProperty("input_tokens") long inputTokens,
			@JsonProperty("output_tokens") long outputTokens,
			@JsonProperty("total_tokens") long totalTokens
	) {
	}
}
