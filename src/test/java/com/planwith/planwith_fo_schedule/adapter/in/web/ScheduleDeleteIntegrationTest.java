package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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
class ScheduleDeleteIntegrationTest {

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
	void keepsRowButExcludesDeletedScheduleFromReadAndUpdate() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/v1/schedules")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "memberUuid": "%s",
								  "destination": "부산",
								  "startDate": "2026-08-20",
								  "endDate": "2026-08-22",
								  "headcount": 2
								}
								""".formatted(UUID.randomUUID())))
				.andExpect(status().isCreated())
				.andReturn();
		JsonNode body = objectMapper.readTree(createResult.getResponse().getContentAsString());
		UUID scheduleUuid = UUID.fromString(body.path("data").path("scheduleUuid").asText());

		mockMvc.perform(delete("/api/v1/schedules/{scheduleUuid}", scheduleUuid))
				.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();
		LocalDateTime deletedAt = entityManager.createQuery("""
				select s.deletedAt
				from ScheduleJpaEntity s
				where s.scheduleUuid = :scheduleUuid
				""", LocalDateTime.class)
				.setParameter("scheduleUuid", scheduleUuid)
				.getSingleResult();
		assertThat(deletedAt).isNotNull();

		mockMvc.perform(get("/schedules/{scheduleUuid}", scheduleUuid))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("SCHEDULE_NOT_FOUND"));

		mockMvc.perform(patch("/schedules/{scheduleUuid}", scheduleUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"삭제 후 수정\"}"))
				.andExpect(status().isNotFound());

		mockMvc.perform(delete("/schedules/{scheduleUuid}", scheduleUuid))
				.andExpect(status().isNotFound());
	}
}
