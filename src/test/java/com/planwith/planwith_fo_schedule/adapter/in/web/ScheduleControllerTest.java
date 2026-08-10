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
	void createsSchedule() throws Exception {
		UUID scheduleId = UUID.randomUUID();
		UUID ownerId = UUID.randomUUID();
		when(createScheduleUseCase.createSchedule(any())).thenReturn(
				new CreateScheduleResult(scheduleId, ownerId, "프로젝트 일정", 1)
		);

		mockMvc.perform(post("/api/v1/schedules")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "ownerId": "%s",
								  "title": "프로젝트 일정",
								  "items": [{
								    "title": "팀 회의",
								    "startsAt": "2026-08-10T01:00:00Z",
								    "endsAt": "2026-08-10T02:00:00Z"
								  }]
								}
								""".formatted(ownerId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.scheduleId").value(scheduleId.toString()))
				.andExpect(jsonPath("$.data.itemCount").value(1));
	}

	@Test
	void rejectsMissingOwnerId() throws Exception {
		mockMvc.perform(post("/api/v1/schedules")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "프로젝트 일정",
								  "items": []
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.error.fieldErrors.ownerId").exists());
	}
}
