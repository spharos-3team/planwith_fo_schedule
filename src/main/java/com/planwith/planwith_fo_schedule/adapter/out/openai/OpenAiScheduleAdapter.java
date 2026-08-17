package com.planwith.planwith_fo_schedule.adapter.out.openai;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_schedule.application.command.AiScheduleGenerateCommand;
import com.planwith.planwith_fo_schedule.application.exception.AiScheduleGenerationException;
import com.planwith.planwith_fo_schedule.application.model.OpenAiUsage;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleGenerationPort;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleRevisionPort;
import com.planwith.planwith_fo_schedule.config.OpenAiProperties;

@Component
public class OpenAiScheduleAdapter implements AiScheduleGenerationPort, AiScheduleRevisionPort {

	private static final Logger log = LoggerFactory.getLogger(OpenAiScheduleAdapter.class);

	private final RestClient openAiRestClient;
	private final OpenAiProperties properties;
	private final OpenAiSchedulePromptFactory promptFactory;
	private final ObjectMapper objectMapper;

	public OpenAiScheduleAdapter(
			@Qualifier("openAiRestClient") RestClient openAiRestClient,
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
		OpenAiTextResult result = requestOutputText(
				promptFactory.instructions(),
				promptFactory.userInput(command),
				"planwith_schedule",
				OpenAiScheduleSchema.value(),
				"generate",
				"일정 생성"
		);
		return toGeneratedSchedule(result.outputText(), result.usage());
	}

	@Override
	public RevisedSchedule revise(ScheduleRevisionContext context) {
		OpenAiTextResult result = requestOutputText(
				promptFactory.revisionInstructions(),
				promptFactory.revisionUserInput(context),
				"planwith_schedule_revision",
				OpenAiScheduleRevisionSchema.value(),
				"revise",
				"일정 첨삭"
		);
		return toRevisedSchedule(result.outputText(), result.usage());
	}

	private OpenAiTextResult requestOutputText(
			String instructions,
			String input,
			String schemaName,
			Map<String, Object> schema,
			String operation,
			String roleDescription
	) {
		validateConfiguration();
		OpenAiResponsesRequest request = new OpenAiResponsesRequest(
				properties.getModel(),
				instructions,
				input,
				new OpenAiResponsesRequest.TextConfiguration(
						new OpenAiResponsesRequest.StructuredFormat(
								"json_schema",
								schemaName,
								true,
								schema
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
			String outputText = response == null ? null : response.outputText();
			if (outputText == null) {
				throw new AiScheduleGenerationException("OpenAI returned no schedule content.");
			}
			OpenAiUsage usage = toUsage(response);
			log.info("OpenAiScheduleAdapter : {} : OpenAI {} 사용량 수집 완료 - model={}, inputTokens={}, "
							+ "outputTokens={}, totalTokens={}",
					operation, roleDescription, usage.model(), usage.inputTokens(), usage.outputTokens(),
					usage.totalTokens());
			return new OpenAiTextResult(outputText, usage);
		} catch (RestClientResponseException exception) {
			log.warn("OpenAiScheduleAdapter : {} : OpenAI {} 요청 실패 - status={}",
					operation, roleDescription, exception.getStatusCode().value());
			throw new AiScheduleGenerationException("OpenAI rejected the schedule generation request.", exception);
		} catch (RestClientException exception) {
			log.warn("OpenAiScheduleAdapter : {} : OpenAI 통신 오류로 {} 요청 실패",
					operation, roleDescription, exception);
			throw new AiScheduleGenerationException("Failed to communicate with OpenAI.", exception);
		}
	}

	private GeneratedAiSchedule toGeneratedSchedule(String outputText, OpenAiUsage usage) {
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
			return new GeneratedAiSchedule(payload.title(), payload.content(), items, usage);
		} catch (JsonProcessingException exception) {
			throw new AiScheduleGenerationException("OpenAI returned an invalid schedule response.", exception);
		}
	}

	private RevisedSchedule toRevisedSchedule(String outputText, OpenAiUsage usage) {
		try {
			OpenAiRevisedSchedulePayload payload = objectMapper.readValue(
					outputText,
					OpenAiRevisedSchedulePayload.class
			);
			if (payload.content() == null || payload.content().isBlank()) {
				throw new AiScheduleGenerationException("OpenAI returned an invalid schedule revision.");
			}
			return new RevisedSchedule(payload.content(), usage);
		} catch (JsonProcessingException exception) {
			throw new AiScheduleGenerationException("OpenAI returned an invalid schedule revision response.", exception);
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

	private OpenAiUsage toUsage(OpenAiResponsesResponse response) {
		if (response.model() == null || response.model().isBlank() || response.usage() == null) {
			throw new AiScheduleGenerationException("OpenAI returned no usage information.");
		}
		try {
			return new OpenAiUsage(
					response.model(),
					response.usage().inputTokens(),
					response.usage().outputTokens(),
					response.usage().totalTokens()
			);
		} catch (IllegalArgumentException exception) {
			throw new AiScheduleGenerationException("OpenAI returned invalid usage information.", exception);
		}
	}

	private record OpenAiTextResult(String outputText, OpenAiUsage usage) {
	}
}
