package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase.CreateScheduleResult;

class ScheduleControllerTest {

	private CreateScheduleUseCase createScheduleUseCase;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		createScheduleUseCase = mock(CreateScheduleUseCase.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new ScheduleController(createScheduleUseCase))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void mapsSqlAlignedScheduleRequestToUseCase() throws Exception {
		UUID scheduleUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		when(createScheduleUseCase.createSchedule(any())).thenReturn(
				new CreateScheduleResult(scheduleUuid, memberUuid, "서울 여행", 1)
		);

		mockMvc.perform(post("/api/v1/schedules")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "memberUuid": "%s",
								  "title": "서울 여행",
								  "destination": "서울",
								  "startDate": "2026-08-10",
								  "endDate": "2026-08-12",
								  "headcount": 2,
								  "expectedCost": 300000,
								  "transportation": "대중교통",
								  "content": "여름 휴가",
								  "calendarColor": "#3366FF",
								  "creatorType": "SELF",
								  "items": [{
								    "dayNumber": 1,
								    "scheduleTime": "10:30:00",
								    "subtitle": "경복궁 관람",
								    "scheduleType": "TOUR",
								    "description": "경복궁을 관람합니다.",
								    "estimatedCost": 3000,
								    "placeName": "경복궁",
								    "placeAddress": "서울특별시 종로구 사직로 161",
								    "latitude": 37.5796170,
								    "longitude": 126.9770410
								  }]
								}
								""".formatted(memberUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.scheduleUuid").value(scheduleUuid.toString()))
				.andExpect(jsonPath("$.data.memberUuid").value(memberUuid.toString()))
				.andExpect(jsonPath("$.data.itemCount").value(1));
	}

	@Test
	void rejectsMissingMemberUuid() throws Exception {
		mockMvc.perform(post("/api/v1/schedules")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "서울 여행",
								  "destination": "서울",
								  "startDate": "2026-08-10",
								  "endDate": "2026-08-12",
								  "creatorType": "SELF",
								  "items": []
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.error.fieldErrors.memberUuid").exists());
	}
}
