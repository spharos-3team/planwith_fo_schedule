package com.planwith.planwith_fo_schedule.adapter.out.openai;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_schedule.application.command.AiScheduleGenerateCommand;
import com.planwith.planwith_fo_schedule.application.exception.AiScheduleGenerationException;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleGenerationPort;
import com.planwith.planwith_fo_schedule.config.OpenAiProperties;

@Component
public class OpenAiScheduleAdapter implements AiScheduleGenerationPort {

	private static final Logger log = LoggerFactory.getLogger(OpenAiScheduleAdapter.class);

	private final RestClient openAiRestClient;
	private final OpenAiProperties properties;
	private final OpenAiSchedulePromptFactory promptFactory;
	private final ObjectMapper objectMapper;

	public OpenAiScheduleAdapter(
			RestClient openAiRestClient,
			OpenAiProperties properties,
			OpenAiSchedulePromptFactory promptFactory,
			ObjectMapper objectMapper
	) {
		this.openAiRestClient = openAiRestClient;
		this.properties = properties;
		this.promptFactory = promptFactory;
		this.objectMapper = objectMapper;
	}

	@Override
	public GeneratedAiSchedule generate(AiScheduleGenerateCommand command) {
		validateConfiguration();
		OpenAiResponsesRequest request = new OpenAiResponsesRequest(
				properties.getModel(),
				promptFactory.instructions(),
				promptFactory.userInput(command),
				new OpenAiResponsesRequest.TextConfiguration(
						new OpenAiResponsesRequest.StructuredFormat(
								"json_schema",
								"planwith_schedule",
								true,
								OpenAiScheduleSchema.value()
						)
				)
		);

		try {
			OpenAiResponsesResponse response = openAiRestClient.post()
					.uri("/v1/responses")
					.contentType(MediaType.APPLICATION_JSON)
					.headers(headers -> headers.setBearerAuth(properties.getApiKey()))
					.body(request)
					.retrieve()
					.body(OpenAiResponsesResponse.class);
			return toGeneratedSchedule(response);
		} catch (RestClientResponseException exception) {
			log.warn("OpenAI schedule request failed with status {}", exception.getStatusCode().value());
			throw new AiScheduleGenerationException("OpenAI rejected the schedule generation request.", exception);
		} catch (RestClientException exception) {
			log.warn("OpenAI schedule request failed due to a communication error", exception);
			throw new AiScheduleGenerationException("Failed to communicate with OpenAI.", exception);
		}
	}

	private GeneratedAiSchedule toGeneratedSchedule(OpenAiResponsesResponse response) {
		String outputText = response == null ? null : response.outputText();
		if (outputText == null) {
			throw new AiScheduleGenerationException("OpenAI returned no schedule content.");
		}

		try {
			OpenAiGeneratedSchedulePayload payload = objectMapper.readValue(
					outputText,
					OpenAiGeneratedSchedulePayload.class
			);
			List<GeneratedScheduleItem> items = payload.items() == null
					? List.of()
					: payload.items().stream()
							.map(item -> new GeneratedScheduleItem(
									item.dayNumber(), item.scheduleTime(), item.subtitle(), item.scheduleType(),
									item.description(), item.estimatedCost(), item.placeName(), item.placeAddress(),
									item.latitude(), item.longitude()
							))
							.toList();
			return new GeneratedAiSchedule(payload.title(), payload.content(), items);
		} catch (JsonProcessingException exception) {
			throw new AiScheduleGenerationException("OpenAI returned an invalid schedule response.", exception);
		}
	}

	private void validateConfiguration() {
		if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
			throw new AiScheduleGenerationException("OPENAI_API_KEY is not configured.");
		}
		if (properties.getModel() == null || properties.getModel().isBlank()) {
			throw new AiScheduleGenerationException("OPENAI_MODEL is not configured.");
		}
	}
}
