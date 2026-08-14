package com.planwith.planwith_fo_schedule.adapter.out.openai;

import org.springframework.stereotype.Component;

@Component
class OpenAiDestinationImagePromptFactory {

	String input(String destination) {
		return """
				Search for exactly one representative travel image for the following destination: %s

				Use the most specific destination information available. Prefer a recognizable landmark, cityscape,
				tourist attraction, or natural scenery strongly associated with the destination. Avoid advertisements,
				logos, maps, icons, unrelated stock images, low-quality thumbnails, and images focused on individuals.
				Return an actual image search result. Never fabricate, infer, construct, or guess an image URL.
				""".formatted(destination);
	}
}
