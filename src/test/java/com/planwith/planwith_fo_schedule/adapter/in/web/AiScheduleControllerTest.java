package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.planwith.planwith_fo_schedule.application.port.in.GenerateAiScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.GenerateAiScheduleUseCase.AiScheduleItemResult;
import com.planwith.planwith_fo_schedule.application.port.in.GenerateAiScheduleUseCase.AiScheduleResult;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

class AiScheduleControllerTest {

	private GenerateAiScheduleUseCase useCase;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		useCase = mock(GenerateAiScheduleUseCase.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new AiScheduleController(useCase))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void generatesAiScheduleWithAuthenticatedMemberHeader() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		when(useCase.generate(any())).thenReturn(new AiScheduleResult(
				memberUuid, "부산 AI 여행", "부산",
				LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 22),
				2, 500_000, TransportationType.TRAIN_PUBLIC_TRANSIT, TravelStyle.TOUR_LANDMARK,
				"AI 추천 일정",
				List.of(new AiScheduleItemResult(
						1, LocalTime.of(10, 0), "해운대 산책", ScheduleItemType.TOUR,
						"해변 산책", 0, "해운대", "부산광역시", new BigDecimal("35.1587000"),
						new BigDecimal("129.1604000")
				))
		));

		mockMvc.perform(post("/api/v1/schedules/ai/generate")
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRequest()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.memberUuid").value(memberUuid.toString()))
				.andExpect(jsonPath("$.data.items[0].scheduleType").value("TOUR"));

		verify(useCase).generate(any());
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
						.header("X-Member-UUID", UUID.randomUUID())
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
}
