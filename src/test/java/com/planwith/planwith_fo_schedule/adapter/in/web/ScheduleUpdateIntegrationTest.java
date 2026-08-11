package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScheduleUpdateIntegrationTest {

	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EntityManager entityManager;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
	}

	@Test
	void createsUpdatesAndReadsScheduleWithoutChangingIdentity() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		MvcResult createResult = mockMvc.perform(post("/api/v1/schedules")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "memberUuid": "%s",
								  "title": "부산 여행",
								  "destination": "부산",
								  "startDate": "2026-09-01",
								  "endDate": "2026-09-03",
								  "headcount": 2,
								  "expectedCost": 500000,
								  "content": "해운대 방문"
								}
								""".formatted(memberUuid)))
				.andExpect(status().isCreated())
				.andReturn();
		JsonNode createBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
		UUID scheduleUuid = UUID.fromString(createBody.path("data").path("scheduleUuid").asText());

		mockMvc.perform(patch("/api/v1/schedules/{scheduleUuid}", scheduleUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "제주 가족여행",
								  "destination": "제주",
								  "endDate": "2026-09-04",
								  "headcount": 3,
								  "expectedCost": 700000,
								  "transportation": "렌터카",
								  "content": "가족 자유여행",
								  "calendarColor": "#22AA88"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.scheduleUuid").value(scheduleUuid.toString()))
				.andExpect(jsonPath("$.data.destination").value("제주"))
				.andExpect(jsonPath("$.data.startDate").value("2026-09-01"))
				.andExpect(jsonPath("$.data.endDate").value("2026-09-04"))
				.andExpect(jsonPath("$.data.creatorType").value("USER"));

		entityManager.flush();
		entityManager.clear();
		Object[] stored = entityManager.createQuery("""
				select s.memberUuid, s.title, s.destination, s.headcount, s.expectedCost, s.creatorType
				from ScheduleJpaEntity s
				where s.scheduleUuid = :scheduleUuid
				""", Object[].class)
				.setParameter("scheduleUuid", scheduleUuid)
				.getSingleResult();
		assertThat(stored).containsExactly(memberUuid, "제주 가족여행", "제주", 3, 700_000L,
				com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType.USER);

		mockMvc.perform(get("/schedules/{scheduleUuid}", scheduleUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.title").value("제주 가족여행"))
				.andExpect(jsonPath("$.data.destination").value("제주"))
				.andExpect(jsonPath("$.data.headcount").value(3))
				.andExpect(jsonPath("$.data.content").value("가족 자유여행"));
	}
}
