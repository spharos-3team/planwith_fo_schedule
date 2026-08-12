package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AiScheduleSaveIntegrationTest {

	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private EntityManager entityManager;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
	}

	@Test
	void savesConfirmedAiDraftAndItemsInDatabase() throws Exception {
		UUID memberUuid = UUID.randomUUID();

		mockMvc.perform(post("/api/v1/schedules/ai/save")
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validSaveRequest()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.scheduleUuid").isNotEmpty())
				.andExpect(jsonPath("$.data.memberUuid").value(memberUuid.toString()))
				.andExpect(jsonPath("$.data.itemCount").value(3));

		entityManager.flush();
		Object[] storedSchedule = entityManager.createQuery("""
				select s.memberUuid, s.creatorType, s.title
				from ScheduleJpaEntity s
				""", Object[].class).getSingleResult();
		assertThat(storedSchedule).containsExactly(memberUuid, ScheduleCreatorType.AI, "부산 AI 여행");

		Long itemCount = entityManager.createQuery(
				"select count(i) from ScheduleItemJpaEntity i",
				Long.class
		).getSingleResult();
		assertThat(itemCount).isEqualTo(3L);
	}

	@Test
	void rejectsDraftMissingTravelDayWithoutSavingAnything() throws Exception {
		mockMvc.perform(post("/api/v1/schedules/ai/save")
						.header("X-Member-UUID", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content(saveRequestWithItems("""
								%s,
								%s
								""".formatted(
								item(1, "10:00:00", "해운대 산책"),
								item(3, "11:00:00", "부산역 이동")
						))))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.error.code").value("INVALID_SCHEDULE"));

		Long scheduleCount = entityManager.createQuery(
				"select count(s) from ScheduleJpaEntity s",
				Long.class
		).getSingleResult();
		assertThat(scheduleCount).isZero();
	}

	private String validSaveRequest() {
		return saveRequestWithItems("""
				%s,
				%s,
				%s
				""".formatted(
				item(1, "10:00:00", "해운대 산책"),
				item(2, "14:00:00", "감천문화마을"),
				item(3, "11:00:00", "부산역 이동")
		));
	}

	private String saveRequestWithItems(String items) {
		return """
				{
				  "title": "부산 AI 여행",
				  "destination": "부산",
				  "startDate": "2026-08-20",
				  "endDate": "2026-08-22",
				  "participantCount": 2,
				  "estimatedBudget": 500000,
				  "transportation": "TRAIN_PUBLIC_TRANSIT",
				  "travelStyle": "TOUR_LANDMARK",
				  "calendarColor": "#4F46E5",
				  "items": [
				    %s
				  ]
				}
				""".formatted(items);
	}

	private String item(int dayNumber, String scheduleTime, String subtitle) {
		return """
				{
				  "dayNumber": %d,
				  "scheduleTime": "%s",
				  "subtitle": "%s",
				  "scheduleType": "TOUR",
				  "description": "AI가 생성한 세부 일정",
				  "estimatedCost": 0,
				  "placeName": null,
				  "placeAddress": null,
				  "latitude": null,
				  "longitude": null
				}
				""".formatted(dayNumber, scheduleTime, subtitle).trim();
	}
}
