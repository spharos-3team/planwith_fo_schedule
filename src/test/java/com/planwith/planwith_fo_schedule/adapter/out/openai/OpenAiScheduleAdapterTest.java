package com.planwith.planwith_fo_schedule.adapter.out.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_schedule.application.command.AiScheduleGenerateCommand;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleRevisionPort.ScheduleRevisionContext;
import com.planwith.planwith_fo_schedule.config.OpenAiProperties;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;

class OpenAiScheduleAdapterTest {

	private ObjectMapper objectMapper;
	private MockRestServiceServer server;
	private OpenAiScheduleAdapter adapter;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper().findAndRegisterModules();
		RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com");
		server = MockRestServiceServer.bindTo(builder).build();
		OpenAiProperties properties = new OpenAiProperties();
		properties.setApiKey("test-api-key");
		properties.setModel("gpt-4o-mini");
		adapter = new OpenAiScheduleAdapter(
				builder.build(),
				properties,
				new OpenAiSchedulePromptFactory(objectMapper),
				objectMapper
		);
	}

	@Test
	void requestsStructuredOutputAndMapsResponse() throws Exception {
		String generatedJson = """
				{
				  "title": "부산 AI 여행",
				  "content": null,
				  "items": [{
				    "dayNumber": 1,
				    "scheduleTime": "10:00:00",
				    "subtitle": "해운대 산책",
				    "scheduleType": "TOUR",
				    "description": "해변 산책",
				    "estimatedCost": 0,
				    "placeName": "해운대",
				    "placeAddress": "부산광역시",
				    "latitude": null,
				    "longitude": null
				  }]
				}
				""";
		String response = """
				{
				  "output": [{
				    "content": [{"type": "output_text", "text": %s}]
				  }]
				}
				""".formatted(objectMapper.writeValueAsString(generatedJson));

		server.expect(requestTo("https://api.openai.com/v1/responses"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-api-key"))
				.andExpect(jsonPath("$.model").value("gpt-4o-mini"))
				.andExpect(jsonPath("$.text.format.type").value("json_schema"))
				.andExpect(jsonPath("$.text.format.strict").value(true))
				.andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

		var result = adapter.generate(command());

		assertThat(result.title()).isEqualTo("부산 AI 여행");
		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).scheduleType()).isEqualTo(ScheduleItemType.TOUR);
		server.verify();
	}

	@Test
	void requestsStructuredRevisionAndMapsResponse() throws Exception {
		String revisedJson = """
				{
				  "content": "해운대를 중심으로 여유롭게 여행합니다."
				}
				""";
		String response = """
				{
				  "output": [{
				    "content": [{"type": "output_text", "text": %s}]
				  }]
				}
				""".formatted(objectMapper.writeValueAsString(revisedJson));

		server.expect(requestTo("https://api.openai.com/v1/responses"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-api-key"))
				.andExpect(jsonPath("$.text.format.name").value("planwith_schedule_revision"))
				.andExpect(jsonPath("$.text.format.strict").value(true))
				.andExpect(jsonPath("$.input").value(org.hamcrest.Matchers.containsString("currentSchedule")))
				.andExpect(jsonPath("$.input").value(org.hamcrest.Matchers.containsString("title")))
				.andExpect(jsonPath("$.input").value(org.hamcrest.Matchers.containsString("destination")))
				.andExpect(jsonPath("$.input").value(org.hamcrest.Matchers.containsString("startDate")))
				.andExpect(jsonPath("$.input").value(org.hamcrest.Matchers.containsString("endDate")))
				.andExpect(jsonPath("$.input").value(org.hamcrest.Matchers.containsString("headcount")))
				.andExpect(jsonPath("$.input").value(org.hamcrest.Matchers.containsString("expectedCost")))
				.andExpect(jsonPath("$.input").value(org.hamcrest.Matchers.containsString("transportation")))
				.andExpect(jsonPath("$.input").value(org.hamcrest.Matchers.containsString("content")))
				.andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

		var result = adapter.revise(revisionContext());

		assertThat(result.content()).contains("해운대");
		server.verify();
	}

	private AiScheduleGenerateCommand command() {
		return new AiScheduleGenerateCommand(
				new MemberUuid(UUID.randomUUID()),
				"부산",
				new SchedulePeriod(LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 22)),
				new Headcount(2),
				ScheduleCost.of(500_000),
				TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.TOUR_LANDMARK,
				"바다 중심 일정",
				null
		);
	}

	private ScheduleRevisionContext revisionContext() {
		return new ScheduleRevisionContext(
				"부산 여행",
				"부산",
				LocalDate.of(2026, 8, 20),
				LocalDate.of(2026, 8, 22),
				2,
				500_000,
				TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.TOUR_LANDMARK,
				"해운대와 광안리를 방문합니다.",
				"바다 중심으로 더 자세히 작성해줘"
		);
	}
}
