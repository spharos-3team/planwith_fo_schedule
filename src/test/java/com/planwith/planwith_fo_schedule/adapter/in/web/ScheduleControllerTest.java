package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
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
import com.planwith.planwith_fo_schedule.application.exception.AuthenticationRequiredException;
import com.planwith.planwith_fo_schedule.application.exception.ScheduleNotFoundException;
import com.planwith.planwith_fo_schedule.application.port.in.GetScheduleDetailUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.GetCalendarSchedulesUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.GetCalendarSchedulesUseCase.CalendarScheduleResult;
import com.planwith.planwith_fo_schedule.application.port.in.DeleteScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.GetScheduleDetailUseCase.ScheduleDetailResult;
import com.planwith.planwith_fo_schedule.application.port.in.GetScheduleDetailUseCase.ScheduleResult;
import com.planwith.planwith_fo_schedule.application.port.in.UpdateScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.UpdateScheduleUseCase.UpdateScheduleCommand;
import com.planwith.planwith_fo_schedule.application.port.in.UpdateScheduleUseCase.UpdateScheduleResult;
import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

class ScheduleControllerTest {

	private CreateScheduleUseCase createScheduleUseCase;
	private GetScheduleDetailUseCase getScheduleDetailUseCase;
	private UpdateScheduleUseCase updateScheduleUseCase;
	private DeleteScheduleUseCase deleteScheduleUseCase;
	private GetCalendarSchedulesUseCase getCalendarSchedulesUseCase;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		createScheduleUseCase = mock(CreateScheduleUseCase.class);
		getScheduleDetailUseCase = mock(GetScheduleDetailUseCase.class);
		updateScheduleUseCase = mock(UpdateScheduleUseCase.class);
		deleteScheduleUseCase = mock(DeleteScheduleUseCase.class);
		getCalendarSchedulesUseCase = mock(GetCalendarSchedulesUseCase.class);
		mockMvc = MockMvcBuilders.standaloneSetup(
				new ScheduleController(
						createScheduleUseCase,
						getScheduleDetailUseCase,
						updateScheduleUseCase,
						deleteScheduleUseCase,
						getCalendarSchedulesUseCase
				)
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
								  "transportation": "TRAIN_PUBLIC_TRANSIT",
								  "travelStyle": "TOUR_LANDMARK",
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
	void rejectsUnsupportedTransportationType() throws Exception {
		mockMvc.perform(post("/api/v1/schedules")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "memberUuid": "%s",
								  "destination": "서울",
								  "startDate": "2026-08-10",
								  "endDate": "2026-08-12",
								  "headcount": 1,
								  "transportation": "KTX"
								}
								""".formatted(UUID.randomUUID())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
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
				new ScheduleResult(
						scheduleUuid, "부산 여행", "부산", "https://images.example.com/busan.jpg",
						LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3), 2, 500_000L,
						TransportationType.TRAIN_PUBLIC_TRANSIT, TravelStyle.TOUR_LANDMARK,
						"해운대 방문", "#3366FF", ScheduleCreatorType.USER
				),
				null,
				List.of()
		));

		mockMvc.perform(get("/schedules/{scheduleUuid}", scheduleUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.schedule.scheduleUuid").value(scheduleUuid.toString()))
				.andExpect(jsonPath("$.data.schedule.title").value("부산 여행"))
				.andExpect(jsonPath("$.data.schedule.destination").value("부산"))
				.andExpect(jsonPath("$.data.schedule.imageUrl").value("https://images.example.com/busan.jpg"))
				.andExpect(jsonPath("$.data.schedule.startDate").value("2026-09-01"))
				.andExpect(jsonPath("$.data.schedule.endDate").value("2026-09-03"))
				.andExpect(jsonPath("$.data.schedule.headcount").value(2))
				.andExpect(jsonPath("$.data.schedule.expectedCost").value(500000))
				.andExpect(jsonPath("$.data.schedule.transportation").value("TRAIN_PUBLIC_TRANSIT"))
				.andExpect(jsonPath("$.data.schedule.travelStyle").value("TOUR_LANDMARK"))
				.andExpect(jsonPath("$.data.schedule.creatorType").value("USER"))
				.andExpect(jsonPath("$.data.flight").doesNotExist())
				.andExpect(jsonPath("$.data.items").isEmpty());
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

	@Test
	void updatesOnlyRequestedScheduleFields() throws Exception {
		UUID scheduleUuid = UUID.randomUUID();
		when(updateScheduleUseCase.updateSchedule(any(), any())).thenReturn(new UpdateScheduleResult(
				scheduleUuid,
				"제주 휴가",
				"제주",
				LocalDate.of(2026, 10, 1),
				LocalDate.of(2026, 10, 4),
				3,
				700_000L,
				TransportationType.RENTAL_CAR,
				TravelStyle.RELAXATION_HEALING,
				"가족 자유여행",
				"#22AA88",
				ScheduleCreatorType.USER
		));

		mockMvc.perform(patch("/api/v1/schedules/{scheduleUuid}", scheduleUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "제주 휴가",
								  "headcount": 3,
								  "content": "가족 자유여행"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.scheduleUuid").value(scheduleUuid.toString()))
				.andExpect(jsonPath("$.data.title").value("제주 휴가"))
				.andExpect(jsonPath("$.data.headcount").value(3))
				.andExpect(jsonPath("$.data.creatorType").value("USER"));

		ArgumentCaptor<UpdateScheduleCommand> commandCaptor = ArgumentCaptor.forClass(UpdateScheduleCommand.class);
		verify(updateScheduleUseCase).updateSchedule(org.mockito.ArgumentMatchers.eq(scheduleUuid), commandCaptor.capture());
		assertThat(commandCaptor.getValue().title()).isEqualTo("제주 휴가");
		assertThat(commandCaptor.getValue().headcount()).isEqualTo(3);
		assertThat(commandCaptor.getValue().destination()).isNull();
	}

	@Test
	void rejectsInvalidUpdateValues() throws Exception {
		mockMvc.perform(patch("/schedules/{scheduleUuid}", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "destination": "   ",
								  "headcount": 0,
								  "expectedCost": -1
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.error.fieldErrors.destination").exists())
				.andExpect(jsonPath("$.error.fieldErrors.headcount").exists())
				.andExpect(jsonPath("$.error.fieldErrors.expectedCost").exists());
	}

	@Test
	void softDeletesSchedule() throws Exception {
		UUID scheduleUuid = UUID.randomUUID();

		mockMvc.perform(delete("/api/v1/schedules/{scheduleUuid}", scheduleUuid))
				.andExpect(status().isNoContent());

		verify(deleteScheduleUseCase).deleteSchedule(scheduleUuid);
	}

	@Test
	void returnsNotFoundWhenDeletingMissingSchedule() throws Exception {
		UUID scheduleUuid = UUID.randomUUID();
		org.mockito.Mockito.doThrow(new ScheduleNotFoundException(scheduleUuid))
				.when(deleteScheduleUseCase).deleteSchedule(scheduleUuid);

		mockMvc.perform(delete("/schedules/{scheduleUuid}", scheduleUuid))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("SCHEDULE_NOT_FOUND"));
	}

	@Test
	void returnsCalendarSchedulesForRequestedPeriod() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID firstUuid = UUID.randomUUID();
		UUID secondUuid = UUID.randomUUID();
		when(getCalendarSchedulesUseCase.getCalendarSchedules(
				memberUuid,
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 31)
		)).thenReturn(List.of(
				new CalendarScheduleResult(
						firstUuid,
						"오사카 여행",
						LocalDate.of(2026, 8, 10),
						LocalDate.of(2026, 8, 13),
						"#3366FF",
						ScheduleCreatorType.USER
				),
				new CalendarScheduleResult(
						secondUuid,
						"제주 여행",
						LocalDate.of(2026, 8, 20),
						LocalDate.of(2026, 8, 22),
						"#22AA88",
						ScheduleCreatorType.AI
				)
		));

		mockMvc.perform(get("/api/v1/schedules/calendar")
						.header("X-Auth-User-Id", memberUuid)
						.param("startDate", "2026-08-01")
						.param("endDate", "2026-08-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(2))
				.andExpect(jsonPath("$.data[0].scheduleUuid").value(firstUuid.toString()))
				.andExpect(jsonPath("$.data[0].title").value("오사카 여행"))
				.andExpect(jsonPath("$.data[0].startDate").value("2026-08-10"))
				.andExpect(jsonPath("$.data[0].calendarColor").value("#3366FF"))
				.andExpect(jsonPath("$.data[0].creatorType").value("USER"));
	}

	@Test
	void rejectsMissingCalendarPeriodParameter() throws Exception {
		mockMvc.perform(get("/schedules/calendar")
						.header("X-Auth-User-Id", UUID.randomUUID())
						.param("startDate", "2026-08-01"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
	}

	@Test
	void rejectsInvalidCalendarDateFormat() throws Exception {
		mockMvc.perform(get("/schedules/calendar")
						.header("X-Auth-User-Id", UUID.randomUUID())
						.param("startDate", "2026/08/01")
						.param("endDate", "2026-08-31"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
	}

	@Test
	void rejectsCalendarQueryWithoutAuthenticatedMember() throws Exception {
		when(getCalendarSchedulesUseCase.getCalendarSchedules(
				null,
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 31)
		)).thenThrow(new AuthenticationRequiredException());

		mockMvc.perform(get("/schedules/calendar")
						.param("startDate", "2026-08-01")
						.param("endDate", "2026-08-31"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
	}
}
