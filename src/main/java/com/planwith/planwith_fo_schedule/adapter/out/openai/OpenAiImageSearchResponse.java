package com.planwith.planwith_fo_schedule.adapter.out.openai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
record OpenAiImageSearchResponse(List<Output> output) {

	String firstImageUrl() {
		if (output == null) {
			return null;
		}
		return output.stream()
				.filter(item -> "web_search_call".equals(item.type()))
				.filter(item -> item.results() != null)
				.flatMap(item -> item.results().stream())
				.filter(result -> "image_result".equals(result.type()))
				.map(ImageResult::imageUrl)
				.filter(url -> url != null && !url.isBlank())
				.findFirst()
				.orElse(null);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Output(String type, List<ImageResult> results) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record ImageResult(
			String type,
			@JsonProperty("image_url") String imageUrl,
			@JsonProperty("source_website_url") String sourceWebsiteUrl,
			@JsonProperty("thumbnail_url") String thumbnailUrl,
			String caption
	) {
	}
}
