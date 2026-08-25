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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AiScheduleSaveIntegrationTest {

	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private ScheduleRepositoryPort scheduleRepositoryPort;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
	}

	@Test
	void savesConfirmedAiDraftAndItemsInDatabase() throws Exception {
		UUID memberUuid = UUID.randomUUID();

		mockMvc.perform(post("/api/v1/schedules/ai/save")
						.header("X-Auth-User-Id", memberUuid)
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

		Long flightCount = entityManager.createQuery(
				"select count(f) from ScheduleFlightJpaEntity f",
				Long.class
		).getSingleResult();
		assertThat(flightCount).isZero();
	}

	@Test
	void savesScheduleItemsSelectedFlightAndSegmentsInOneRequest() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/schedules/ai/save")
						.header("X-Auth-User-Id", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content(saveRequestWithItemsAndFlight("""
								%s,
								%s,
								%s
								""".formatted(
								item(1, "10:00:00", "Arrival"),
								item(2, "14:00:00", "Tokyo tour"),
								item(3, "11:00:00", "Departure")
						))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.itemCount").value(3))
				.andExpect(jsonPath("$.data.flightSaved").value(true))
				.andExpect(jsonPath("$.data.flightSegmentCount").value(2))
				.andReturn();

		entityManager.flush();
		assertThat(count("ScheduleJpaEntity")).isEqualTo(1L);
		assertThat(count("ScheduleItemJpaEntity")).isEqualTo(3L);
		assertThat(count("ScheduleFlightJpaEntity")).isEqualTo(1L);
		assertThat(count("ScheduleFlightSegmentJpaEntity")).isEqualTo(2L);

		UUID scheduleUuid = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
				.path("data").path("scheduleUuid").asText());
		entityManager.clear();
		var loaded = scheduleRepositoryPort.findByScheduleUuid(new ScheduleUuid(scheduleUuid)).orElseThrow();
		assertThat(loaded.items()).hasSize(3);
		assertThat(loaded.flight()).isNotNull();
		assertThat(loaded.flight().segments()).hasSize(2);
		assertThat(loaded.flight().segments().get(0).flightNumber()).isEqualTo("703");
	}

	@Test
	void rejectsReturnCandidatesForOneWayFlightWithoutSavingAnything() throws Exception {
		String invalidRequest = saveRequestWithItemsAndFlight("""
				%s,
				%s,
				%s
				""".formatted(
				item(1, "10:00:00", "Arrival"),
				item(2, "14:00:00", "Tokyo tour"),
				item(3, "11:00:00", "Departure")
		)).replace("\"tripType\": \"ROUND_TRIP\"", "\"tripType\": \"ONE_WAY\"");

		mockMvc.perform(post("/api/v1/schedules/ai/save")
						.header("X-Auth-User-Id", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidRequest))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

		assertThat(count("ScheduleJpaEntity")).isZero();
		assertThat(count("ScheduleFlightJpaEntity")).isZero();
	}

	@Test
	void rejectsDraftMissingTravelDayWithoutSavingAnything() throws Exception {
		mockMvc.perform(post("/api/v1/schedules/ai/save")
						.header("X-Auth-User-Id", UUID.randomUUID())
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

	private String saveRequestWithItemsAndFlight(String items) {
		return """
				{
				  "title": "Tokyo AI trip",
				  "destination": "Tokyo",
				  "startDate": "2026-08-20",
				  "endDate": "2026-08-22",
				  "participantCount": 2,
				  "estimatedBudget": 500000,
				  "transportation": "OTHER",
				  "travelStyle": "TOUR_LANDMARK",
				  "calendarColor": "#4F46E5",
				  "items": [%s],
				  "flight": {
				    "departureLocation": "Seoul",
				    "tripType": "ROUND_TRIP",
				    "outboundCandidates": [%s],
				    "returnCandidates": [%s]
				  }
				}
				""".formatted(
				items,
				flightCandidate("2026-08-20", "ICN", "NRT", "2026-08-20T09:00:00+09:00",
						"2026-08-20T11:30:00+09:00", "703"),
				flightCandidate("2026-08-22", "NRT", "ICN", "2026-08-22T18:00:00+09:00",
						"2026-08-22T20:30:00+09:00", "704")
		);
	}

	private String flightCandidate(
			String flightDate,
			String departureCode,
			String arrivalCode,
			String departureAt,
			String arrivalAt,
			String flightNumber
	) {
		String departureTimezone = "ICN".equals(departureCode) ? "Asia/Seoul" : "Asia/Tokyo";
		String arrivalTimezone = "ICN".equals(arrivalCode) ? "Asia/Seoul" : "Asia/Tokyo";
		return """
				{
				  "flightDate": "%s",
				  "flightStatus": "scheduled",
				  "departure": {
				    "airportCode": "%s", "terminal": "1", "gate": "10",
				    "scheduledAt": "%s", "timezone": "%s"
				  },
				  "arrival": {
				    "airportCode": "%s", "terminal": "2", "gate": "20",
				    "scheduledAt": "%s", "timezone": "%s"
				  },
				  "carrierCode": "KE",
				  "flightNumber": "%s",
				  "operatingCarrierCode": "KE",
				  "aircraftCode": "B789",
				  "durationMinutes": 150
				}
				""".formatted(
				flightDate, departureCode, departureAt, departureTimezone,
				arrivalCode, arrivalAt, arrivalTimezone, flightNumber
		);
	}

	private Long count(String entityName) {
		return entityManager.createQuery("select count(e) from " + entityName + " e", Long.class)
				.getSingleResult();
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
