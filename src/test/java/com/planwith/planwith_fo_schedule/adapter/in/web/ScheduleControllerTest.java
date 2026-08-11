package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import com.planwith.planwith_fo_schedule.application.exception.ScheduleNotFoundException;
import com.planwith.planwith_fo_schedule.application.port.in.GetScheduleDetailUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.GetScheduleDetailUseCase.ScheduleDetailResult;
import com.planwith.planwith_fo_schedule.domain.CreatorType;

class ScheduleControllerTest {

	private CreateScheduleUseCase createScheduleUseCase;
	private GetScheduleDetailUseCase getScheduleDetailUseCase;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		createScheduleUseCase = mock(CreateScheduleUseCase.class);
		getScheduleDetailUseCase = mock(GetScheduleDetailUseCase.class);
		mockMvc = MockMvcBuilders.standaloneSetup(
				new ScheduleController(createScheduleUseCase, getScheduleDetailUseCase)
		)
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

	@Test
	void returnsScheduleDetail() throws Exception {
		UUID scheduleUuid = UUID.randomUUID();
		when(getScheduleDetailUseCase.getScheduleDetail(scheduleUuid)).thenReturn(new ScheduleDetailResult(
				scheduleUuid,
				"부산 여행",
				"부산",
				LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 9, 3),
				2,
				500_000L,
				"대중교통",
				"해운대 방문",
				"#3366FF",
				CreatorType.SELF
		));

		mockMvc.perform(get("/schedules/{scheduleUuid}", scheduleUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.scheduleUuid").value(scheduleUuid.toString()))
				.andExpect(jsonPath("$.data.title").value("부산 여행"))
				.andExpect(jsonPath("$.data.destination").value("부산"))
				.andExpect(jsonPath("$.data.startDate").value("2026-09-01"))
				.andExpect(jsonPath("$.data.endDate").value("2026-09-03"))
				.andExpect(jsonPath("$.data.headcount").value(2))
				.andExpect(jsonPath("$.data.expectedCost").value(500000))
				.andExpect(jsonPath("$.data.creatorType").value("SELF"));
	}

	@Test
	void returnsNotFoundWhenScheduleDoesNotExist() throws Exception {
		UUID scheduleUuid = UUID.randomUUID();
		when(getScheduleDetailUseCase.getScheduleDetail(scheduleUuid))
				.thenThrow(new ScheduleNotFoundException(scheduleUuid));

		mockMvc.perform(get("/schedules/{scheduleUuid}", scheduleUuid))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("SCHEDULE_NOT_FOUND"));
	}

	@Test
	void rejectsInvalidScheduleUuidFormat() throws Exception {
		mockMvc.perform(get("/schedules/not-a-uuid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
	}
}
