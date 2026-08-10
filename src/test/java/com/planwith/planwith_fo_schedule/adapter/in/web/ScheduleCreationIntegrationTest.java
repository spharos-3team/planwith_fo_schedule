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

import com.planwith.planwith_fo_schedule.domain.CreatorType;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScheduleCreationIntegrationTest {

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
	void createsSelfScheduleAndStoresItInDatabase() throws Exception {
		UUID memberUuid = UUID.randomUUID();

		mockMvc.perform(post("/schedules")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "memberUuid": "%s",
								  "destination": "부산",
								  "startDate": "2026-09-01",
								  "endDate": "2026-09-03",
								  "headcount": 2,
								  "content": "해운대에서 자유롭게 시간을 보낸다."
								}
								""".formatted(memberUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.scheduleUuid").isNotEmpty())
				.andExpect(jsonPath("$.data.memberUuid").value(memberUuid.toString()))
				.andExpect(jsonPath("$.data.title").value("부산 여행"));

		Object[] storedSchedule = entityManager.createQuery("""
				select s.destination, s.startDate, s.endDate, s.creatorType, s.content
				from ScheduleJpaEntity s
				""", Object[].class).getSingleResult();
		assertThat(storedSchedule).containsExactly(
				"부산",
				java.time.LocalDate.of(2026, 9, 1),
				java.time.LocalDate.of(2026, 9, 3),
				CreatorType.SELF,
				"해운대에서 자유롭게 시간을 보낸다."
		);
		Long itemCount = entityManager.createQuery(
				"select count(i) from ScheduleItemJpaEntity i",
				Long.class
		).getSingleResult();
		assertThat(itemCount).isZero();
	}

	@Test
	void rejectsInvalidPeriodWithoutStoringSchedule() throws Exception {
		mockMvc.perform(post("/schedules")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "memberUuid": "%s",
								  "destination": "부산",
								  "startDate": "2026-09-03",
								  "endDate": "2026-09-01",
								  "headcount": 2
								}
								""".formatted(UUID.randomUUID())))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("INVALID_SCHEDULE"));

		Long scheduleCount = entityManager.createQuery(
				"select count(s) from ScheduleJpaEntity s",
				Long.class
		).getSingleResult();
		assertThat(scheduleCount).isZero();
	}
}
