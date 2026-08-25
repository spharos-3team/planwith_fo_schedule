package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.planwith.planwith_fo_schedule.application.command.AiScheduleGenerateCommand;
import com.planwith.planwith_fo_schedule.application.model.AiOperationType;
import com.planwith.planwith_fo_schedule.application.model.AiUsageResult;
import com.planwith.planwith_fo_schedule.application.model.OpenAiUsage;
import com.planwith.planwith_fo_schedule.application.port.in.GenerateAiScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.GenerateAiScheduleUseCase.AiScheduleItemResult;
import com.planwith.planwith_fo_schedule.application.port.in.GenerateAiScheduleUseCase.AiScheduleResult;
import com.planwith.planwith_fo_schedule.application.port.in.SaveAiScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.SaveAiScheduleUseCase.SaveAiScheduleResult;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

class AiScheduleControllerTest {

	private GenerateAiScheduleUseCase useCase;
	private SaveAiScheduleUseCase saveUseCase;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		useCase = mock(GenerateAiScheduleUseCase.class);
		saveUseCase = mock(SaveAiScheduleUseCase.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new AiScheduleController(useCase, saveUseCase))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void generatesAiScheduleWithAuthenticatedMemberHeader() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		when(useCase.generate(any(), any())).thenReturn(aiScheduleResult(memberUuid, AiOperationType.GENERATE));

		mockMvc.perform(post("/api/v1/schedules/ai/generate")
						.header("X-Auth-User-Id", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRequest()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.memberUuid").value(memberUuid.toString()))
				.andExpect(jsonPath("$.data.imageUrl").value("https://images.example.com/busan.jpg"))
				.andExpect(jsonPath("$.data.items[0].scheduleType").value("TOUR"))
				.andExpect(jsonPath("$.data.scheduleUsage.model").value("gpt-4o-mini-2024-07-18"))
				.andExpect(jsonPath("$.data.scheduleUsage.inputTokens").value(120))
				.andExpect(jsonPath("$.data.scheduleUsage.outputTokens").value(80))
				.andExpect(jsonPath("$.data.scheduleUsage.totalTokens").value(200))
				.andExpect(jsonPath("$.data.imageUsage.model").value("gpt-5.6-2026-08-01"))
				.andExpect(jsonPath("$.data.imageUsage.totalTokens").value(60))
				.andExpect(jsonPath("$.data.usage.memberUuid").value(memberUuid.toString()))
				.andExpect(jsonPath("$.data.usage.operationType").value("GENERATE"))
				.andExpect(jsonPath("$.data.usage.inputTokens").value(165))
				.andExpect(jsonPath("$.data.usage.outputTokens").value(95))
				.andExpect(jsonPath("$.data.usage.totalTokens").value(260));

		verify(useCase).generate(any(), org.mockito.ArgumentMatchers.eq(AiOperationType.GENERATE));
	}

	@Test
	void generatesNewDraftWithReenteredAndModifiedConditions() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		when(useCase.generate(any(), any())).thenReturn(aiScheduleResult(memberUuid, AiOperationType.GENERATE));

		mockMvc.perform(post("/api/v1/schedules/ai/generate")
						.header("X-Auth-User-Id", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "destination": "제주",
								  "startDate": "2026-09-01",
								  "endDate": "2026-09-05",
								  "participantCount": 4,
								  "estimatedBudget": 1200000,
								  "transportation": "RENTAL_CAR",
								  "travelStyle": "RELAXATION_HEALING",
								  "additionalRequest": "아이와 함께 이동하기 편한 일정"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));

		ArgumentCaptor<AiScheduleGenerateCommand> commandCaptor =
				ArgumentCaptor.forClass(AiScheduleGenerateCommand.class);
		verify(useCase).generate(commandCaptor.capture(), org.mockito.ArgumentMatchers.eq(AiOperationType.GENERATE));
		AiScheduleGenerateCommand command = commandCaptor.getValue();
		assertThat(command.memberUuid().value()).isEqualTo(memberUuid);
		assertThat(command.destination()).isEqualTo("제주");
		assertThat(command.period().startDate())
				.isEqualTo(LocalDate.of(2026, 9, 1));
		assertThat(command.period().endDate())
				.isEqualTo(LocalDate.of(2026, 9, 5));
		assertThat(command.participantCount().value()).isEqualTo(4);
		assertThat(command.estimatedBudget().amount()).isEqualTo(1_200_000L);
		assertThat(command.transportation()).isEqualTo(TransportationType.RENTAL_CAR);
		assertThat(command.travelStyle()).isEqualTo(TravelStyle.RELAXATION_HEALING);
		assertThat(command.additionalRequest())
				.isEqualTo("아이와 함께 이동하기 편한 일정");
	}

	@Test
	void regeneratesAiScheduleByCallingExistingGenerationUseCaseAgain() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		when(useCase.generate(any(), any())).thenReturn(aiScheduleResult(memberUuid, AiOperationType.REGENERATE));

		mockMvc.perform(post("/api/v1/schedules/ai/regenerate")
						.header("X-Auth-User-Id", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRequest()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.memberUuid").value(memberUuid.toString()))
				.andExpect(jsonPath("$.data.items[0].scheduleType").value("TOUR"))
				.andExpect(jsonPath("$.data.usage.operationType").value("REGENERATE"));

		verify(useCase).generate(any(), org.mockito.ArgumentMatchers.eq(AiOperationType.REGENERATE));
	}

	@Test
	void rejectsRegenerationWithoutAuthenticationHeader() throws Exception {
		mockMvc.perform(post("/api/v1/schedules/ai/regenerate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRequest()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
	}

	@Test
	void savesConfirmedAiDraft() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID scheduleUuid = UUID.randomUUID();
		when(saveUseCase.save(any())).thenReturn(
				new SaveAiScheduleResult(scheduleUuid, memberUuid, "부산 AI 여행", 1, false, 0)
		);

		mockMvc.perform(post("/api/v1/schedules/ai/save")
						.header("X-Auth-User-Id", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validSaveRequest()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.scheduleUuid").value(scheduleUuid.toString()))
				.andExpect(jsonPath("$.data.memberUuid").value(memberUuid.toString()))
				.andExpect(jsonPath("$.data.itemCount").value(1));

		verify(saveUseCase).save(any());
		verifyNoInteractions(useCase);
	}

	@Test
	void rejectsAiDraftSaveWithoutAuthenticationHeader() throws Exception {
		mockMvc.perform(post("/api/v1/schedules/ai/save")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validSaveRequest()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
	}

	@Test
	void rejectsRequestWithoutAuthenticationHeader() throws Exception {
		mockMvc.perform(post("/api/v1/schedules/ai/generate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRequest()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
	}

	@Test
	void rejectsInvalidTravelPeriodBeforeCallingUseCase() throws Exception {
		mockMvc.perform(post("/api/v1/schedules/ai/generate")
						.header("X-Auth-User-Id", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRequest().replace("2026-08-22", "2026-08-19")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
	}

	private String validRequest() {
		return """
				{
				  "destination": "부산",
				  "startDate": "2026-08-20",
				  "endDate": "2026-08-22",
				  "participantCount": 2,
				  "estimatedBudget": 500000,
				  "transportation": "TRAIN_PUBLIC_TRANSIT",
				  "travelStyle": "TOUR_LANDMARK",
				  "additionalRequest": "바다 중심 일정"
				}
				""";
	}

	private AiScheduleResult aiScheduleResult(UUID memberUuid, AiOperationType operationType) {
		return new AiScheduleResult(
				memberUuid, "부산 AI 여행", "부산", "https://images.example.com/busan.jpg",
				LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 22),
				2, 500_000, TransportationType.TRAIN_PUBLIC_TRANSIT, TravelStyle.TOUR_LANDMARK,
				"AI 추천 일정",
				List.of(new AiScheduleItemResult(
						1, LocalTime.of(10, 0), "해운대 산책", ScheduleItemType.TOUR,
						"해변 산책", 0, "해운대", "부산광역시", new BigDecimal("35.1587000"),
						new BigDecimal("129.1604000")
				)),
				new OpenAiUsage("gpt-4o-mini-2024-07-18", 120, 80, 200),
				new OpenAiUsage("gpt-5.6-2026-08-01", 45, 15, 60),
				new AiUsageResult(
						memberUuid,
						UUID.randomUUID(),
						operationType,
						"gpt-4o-mini-2024-07-18,gpt-5.6-2026-08-01",
						165,
						95,
						260
				)
		);
	}

	private String validSaveRequest() {
		return """
				{
				  "title": "부산 AI 여행",
				  "destination": "부산",
				  "imageUrl": "https://images.example.com/busan.jpg",
				  "startDate": "2026-08-20",
				  "endDate": "2026-08-22",
				  "participantCount": 2,
				  "estimatedBudget": 500000,
				  "transportation": "TRAIN_PUBLIC_TRANSIT",
				  "travelStyle": "TOUR_LANDMARK",
				  "calendarColor": "#4F46E5",
				  "items": [{
				    "dayNumber": 1,
				    "scheduleTime": "10:00:00",
				    "subtitle": "해운대 산책",
				    "scheduleType": "TOUR",
				    "description": "해변 산책",
				    "estimatedCost": 0,
				    "placeName": "해운대",
				    "placeAddress": "부산광역시 해운대구",
				    "latitude": null,
				    "longitude": null
				  }]
				}
				""";
	}
}
