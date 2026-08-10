package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase.CreateScheduleCommand;
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
	void createsSelfScheduleThroughPublicEndpoint() throws Exception {
		UUID scheduleUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		when(createScheduleUseCase.createSchedule(any())).thenReturn(
				new CreateScheduleResult(scheduleUuid, memberUuid, "서울 여행")
		);

		mockMvc.perform(post("/schedules")
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
								  "calendarColor": "#3366FF"
								}
								""".formatted(memberUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.scheduleUuid").value(scheduleUuid.toString()))
				.andExpect(jsonPath("$.data.memberUuid").value(memberUuid.toString()));

		ArgumentCaptor<CreateScheduleCommand> commandCaptor = ArgumentCaptor.forClass(CreateScheduleCommand.class);
		verify(createScheduleUseCase).createSchedule(commandCaptor.capture());
		assertThat(commandCaptor.getValue().content()).isEqualTo("여름 휴가");
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
								  "endDate": "2026-08-12"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.error.fieldErrors.memberUuid").exists());
	}

	@Test
	void acceptsMissingTitleAndColor() throws Exception {
		UUID scheduleUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		when(createScheduleUseCase.createSchedule(any())).thenReturn(
				new CreateScheduleResult(scheduleUuid, memberUuid, "제주도 여행")
		);

		mockMvc.perform(post("/schedules")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "memberUuid": "%s",
								  "destination": "제주도",
								  "startDate": "2026-08-10",
								  "endDate": "2026-08-12",
								  "headcount": 1
								}
								""".formatted(memberUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.title").value("제주도 여행"));
	}
}
