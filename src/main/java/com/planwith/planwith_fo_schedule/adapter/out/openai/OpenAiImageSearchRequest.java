package com.planwith.planwith_fo_schedule.adapter.out.openai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

record OpenAiImageSearchRequest(
		String model,
		List<WebSearchTool> tools,
		List<String> include,
		String input
) {
	record WebSearchTool(
			String type,
			@JsonProperty("search_content_types") List<String> searchContentTypes,
			@JsonProperty("image_settings") ImageSettings imageSettings
	) {
	}

	record ImageSettings(
			@JsonProperty("max_results") int maxResults,
			boolean caption
	) {
	}
}
