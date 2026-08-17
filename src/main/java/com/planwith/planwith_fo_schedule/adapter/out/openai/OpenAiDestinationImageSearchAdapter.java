package com.planwith.planwith_fo_schedule.adapter.out.openai;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.planwith.planwith_fo_schedule.application.model.OpenAiUsage;
import com.planwith.planwith_fo_schedule.application.port.out.DestinationImageSearchPort;
import com.planwith.planwith_fo_schedule.application.port.out.DestinationImageSearchPort.DestinationImageSearchResult;
import com.planwith.planwith_fo_schedule.config.OpenAiProperties;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleImageUrl;

@Component
public class OpenAiDestinationImageSearchAdapter implements DestinationImageSearchPort {

	private static final Logger log = LoggerFactory.getLogger(OpenAiDestinationImageSearchAdapter.class);
	private final RestClient openAiRestClient;
	private final OpenAiProperties properties;
	private final OpenAiDestinationImagePromptFactory promptFactory;

	public OpenAiDestinationImageSearchAdapter(
			@Qualifier("openAiRestClient") RestClient openAiRestClient,
			OpenAiProperties properties,
			OpenAiDestinationImagePromptFactory promptFactory
	) {
		this.openAiRestClient = openAiRestClient;
		this.properties = properties;
		this.promptFactory = promptFactory;
	}

	@Override
	public Optional<String> searchRepresentativeImage(String destination) {
		return searchRepresentativeImageWithUsage(destination).imageUrl();
	}

	@Override
	public DestinationImageSearchResult searchRepresentativeImageWithUsage(String destination) {
		if (destination == null || destination.isBlank() || properties.getImageSearchModel() == null
				|| properties.getImageSearchModel().isBlank()) {
			log.warn("OpenAiDestinationImageSearchAdapter : searchRepresentativeImage : 대표 이미지 검색 조건이 없어 검색 생략");
			return new DestinationImageSearchResult(Optional.empty(), null);
		}

		OpenAiImageSearchRequest request = new OpenAiImageSearchRequest(
				properties.getImageSearchModel(),
				List.of(new OpenAiImageSearchRequest.WebSearchTool(
						"web_search",
						List.of("image", "text"),
						new OpenAiImageSearchRequest.ImageSettings(1, true)
				)),
				List.of("web_search_call.results"),
				promptFactory.input(destination.trim())
		);

		try {
			log.info("OpenAiDestinationImageSearchAdapter : searchRepresentativeImage : 목적지 대표 이미지 검색 시작");
			OpenAiImageSearchResponse response = openAiRestClient.post()
					.uri("/v1/responses")
					.contentType(MediaType.APPLICATION_JSON)
					.headers(headers -> headers.setBearerAuth(properties.getApiKey()))
					.body(request)
					.retrieve()
					.body(OpenAiImageSearchResponse.class);
			String imageUrl = response == null ? null : response.firstImageUrl();
			OpenAiUsage usage = toUsage(response);
			if (usage != null) {
				log.info("OpenAiDestinationImageSearchAdapter : searchRepresentativeImage : OpenAI 대표 이미지 검색 "
								+ "사용량 수집 완료 - model={}, inputTokens={}, outputTokens={}, totalTokens={}",
						usage.model(), usage.inputTokens(), usage.outputTokens(), usage.totalTokens());
			}
			if (imageUrl == null || !isValidPublicImageUrl(imageUrl)) {
				log.warn("OpenAiDestinationImageSearchAdapter : searchRepresentativeImage : 유효한 대표 이미지 검색 결과 없음");
				return new DestinationImageSearchResult(Optional.empty(), usage);
			}
			log.info("OpenAiDestinationImageSearchAdapter : searchRepresentativeImage : 목적지 대표 이미지 검색 완료");
			return new DestinationImageSearchResult(Optional.of(imageUrl.trim()), usage);
		} catch (RestClientException exception) {
			log.warn("OpenAiDestinationImageSearchAdapter : searchRepresentativeImage : OpenAI 대표 이미지 검색 실패",
					exception);
			return new DestinationImageSearchResult(Optional.empty(), null);
		}
	}

	private boolean isValidPublicImageUrl(String imageUrl) {
		return ScheduleImageUrl.isValid(imageUrl);
	}

	private OpenAiUsage toUsage(OpenAiImageSearchResponse response) {
		if (response == null || response.model() == null || response.model().isBlank() || response.usage() == null) {
			log.warn("OpenAiDestinationImageSearchAdapter : toUsage : OpenAI 대표 이미지 검색 사용량 정보 없음");
			return null;
		}
		try {
			return new OpenAiUsage(
					response.model(),
					response.usage().inputTokens(),
					response.usage().outputTokens(),
					response.usage().totalTokens()
			);
		} catch (IllegalArgumentException exception) {
			log.warn("OpenAiDestinationImageSearchAdapter : toUsage : OpenAI 대표 이미지 검색 사용량 정보 오류");
			return null;
		}
	}
}
