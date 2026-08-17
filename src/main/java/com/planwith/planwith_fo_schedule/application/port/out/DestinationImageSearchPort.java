package com.planwith.planwith_fo_schedule.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_schedule.application.model.OpenAiUsage;

public interface DestinationImageSearchPort {

	Optional<String> searchRepresentativeImage(String destination);

	default DestinationImageSearchResult searchRepresentativeImageWithUsage(String destination) {
		return new DestinationImageSearchResult(searchRepresentativeImage(destination), null);
	}

	record DestinationImageSearchResult(
			Optional<String> imageUrl,
			OpenAiUsage usage
	) {
		public DestinationImageSearchResult {
			imageUrl = imageUrl == null ? Optional.empty() : imageUrl;
		}
	}
}
