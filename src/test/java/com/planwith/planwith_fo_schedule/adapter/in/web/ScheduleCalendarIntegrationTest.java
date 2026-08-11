package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScheduleCalendarIntegrationTest {

	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private ObjectMapper objectMapper;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
	}

	@Test
	void returnsOnlyActiveSchedulesOverlappingCalendarPeriod() throws Exception {
		UUID startsBefore = createSchedule(
				"월초 일정",
				LocalDate.of(2026, 7, 30),
				LocalDate.of(2026, 8, 2),
				"#111111"
		);
		UUID inside = createSchedule(
				"오사카 여행",
				LocalDate.of(2026, 8, 10),
				LocalDate.of(2026, 8, 13),
				"#222222"
		);
		UUID endsAfter = createSchedule(
				"월말 일정",
				LocalDate.of(2026, 8, 30),
				LocalDate.of(2026, 9, 2),
				"#333333"
		);
		createSchedule(
				"지난 일정",
				LocalDate.of(2026, 7, 1),
				LocalDate.of(2026, 7, 5),
				"#444444"
		);
		createSchedule(
				"다음 일정",
				LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 9, 3),
				"#555555"
		);
		UUID deleted = createSchedule(
				"삭제 일정",
				LocalDate.of(2026, 8, 15),
				LocalDate.of(2026, 8, 16),
				"#666666"
		);
		mockMvc.perform(delete("/schedules/{scheduleUuid}", deleted))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/schedules/calendar")
						.param("startDate", "2026-08-01")
						.param("endDate", "2026-08-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(3))
				.andExpect(jsonPath("$.data[0].scheduleUuid").value(startsBefore.toString()))
				.andExpect(jsonPath("$.data[0].title").value("월초 일정"))
				.andExpect(jsonPath("$.data[1].scheduleUuid").value(inside.toString()))
				.andExpect(jsonPath("$.data[1].title").value("오사카 여행"))
				.andExpect(jsonPath("$.data[1].calendarColor").value("#222222"))
				.andExpect(jsonPath("$.data[1].creatorType").value("SELF"))
				.andExpect(jsonPath("$.data[2].scheduleUuid").value(endsAfter.toString()))
				.andExpect(jsonPath("$.data[2].title").value("월말 일정"));
	}

	@Test
	void rejectsInvalidCalendarPeriod() throws Exception {
		mockMvc.perform(get("/schedules/calendar")
						.param("startDate", "2026-08-31")
						.param("endDate", "2026-08-01"))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.error.code").value("INVALID_SCHEDULE"));
	}

	private UUID createSchedule(
			String title,
			LocalDate startDate,
			LocalDate endDate,
			String calendarColor
	) throws Exception {
		MvcResult result = mockMvc.perform(post("/schedules")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "memberUuid": "%s",
								  "title": "%s",
								  "destination": "테스트 목적지",
								  "startDate": "%s",
								  "endDate": "%s",
								  "headcount": 1,
								  "calendarColor": "%s"
								}
								""".formatted(UUID.randomUUID(), title, startDate, endDate, calendarColor)))
				.andExpect(status().isCreated())
				.andReturn();
		return UUID.fromString(
				objectMapper.readTree(result.getResponse().getContentAsString())
						.path("data")
						.path("scheduleUuid")
						.asText()
		);
	}
}
