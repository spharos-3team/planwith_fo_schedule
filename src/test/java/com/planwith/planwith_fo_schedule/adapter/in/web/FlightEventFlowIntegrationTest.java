package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_schedule.application.command.AiScheduleGenerateCommand;
import com.planwith.planwith_fo_schedule.application.exception.FlightSearchException;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate.AirportSchedule;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleGenerationPort.GeneratedAiSchedule;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleGenerationPort.GeneratedScheduleItem;
import com.planwith.planwith_fo_schedule.application.port.out.DestinationImageSearchPort;
import com.planwith.planwith_fo_schedule.application.port.out.FlightRecommendationCachePort;
import com.planwith.planwith_fo_schedule.application.port.out.FlightSearchPort;
import com.planwith.planwith_fo_schedule.application.port.out.FlightSearchPort.FlightSearchCriteria;
import com.planwith.planwith_fo_schedule.adapter.out.openai.OpenAiScheduleAdapter;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FlightEventFlowIntegrationTest {

	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OpenAiScheduleAdapter aiScheduleGenerationPort;

	@MockitoBean
	private DestinationImageSearchPort destinationImageSearchPort;

	@MockitoBean
	private FlightSearchPort flightSearchPort;

	@MockitoBean
	private FlightRecommendationCachePort flightRecommendationCachePort;

	private MockMvc mockMvc;
	private UUID memberUuid;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
		memberUuid = UUID.randomUUID();
		when(destinationImageSearchPort.searchRepresentativeImage(any())).thenReturn(Optional.empty());
		when(flightRecommendationCachePort.find(any())).thenReturn(Optional.empty());
	}

	@Test
	void completesAiIataSearchRecommendationSelectionSaveAndDetailFlow() throws Exception {
		when(aiScheduleGenerationPort.generate(any())).thenReturn(generatedSchedule());
		when(flightSearchPort.search(any())).thenAnswer(invocation ->
				candidatesFor(invocation.getArgument(0))
		);

		// 시나리오 2: 항공편 검색 조건을 포함한 AI 일정 생성
		mockMvc.perform(post("/api/v1/schedules/ai/generate")
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(aiGenerateRequest(true)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.title").value("Tokyo AI trip"))
				.andExpect(jsonPath("$.data.items.length()").value(3));

		// 시나리오 3: 지역명을 실제 공항 IATA 후보로 변환
		mockMvc.perform(get("/api/v1/flight-locations/airports").param("location", "서울"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.airportCodes[0]").value("ICN"))
				.andExpect(jsonPath("$.data.airportCodes[1]").value("GMP"));
		mockMvc.perform(get("/api/v1/flight-locations/airports").param("location", "도쿄"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.airportCodes[0]").value("NRT"))
				.andExpect(jsonPath("$.data.airportCodes[1]").value("HND"));

		// 시나리오 4: 왕복 항공편 검색 성공
		mockMvc.perform(post("/api/v1/flights/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(flightSearchRequest()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.outboundCandidates.length()").value(4))
				.andExpect(jsonPath("$.data.returnCandidates.length()").value(3));

		// 시나리오 6: 추천 정책으로 방향별 TOP 3 반환 및 캐시 저장
		mockMvc.perform(post("/api/v1/flights/recommendations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(flightSearchRequest()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.outboundCandidates.length()").value(3))
				.andExpect(jsonPath("$.data.returnCandidates.length()").value(3));
		verify(flightRecommendationCachePort).save(any(), any());

		// 시나리오 7: 사용자가 추천 항공편을 선택하여 최종 정보 확정
		mockMvc.perform(post("/api/v1/flights/confirmations")
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(confirmationRequest()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.outboundFlight.candidate.flightNumber").value("701"))
				.andExpect(jsonPath("$.data.returnFlight.candidate.flightNumber").value("801"))
				.andExpect(jsonPath("$.data.outboundFlight.refreshed").value(false));

		// 시나리오 8: 확정한 왕복 항공편과 AI 일정을 내 캘린더에 저장
		MvcResult saveResult = mockMvc.perform(post("/api/v1/schedules/ai/save")
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(saveRequestWithDirectRoundTrip()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.flightSaved").value(true))
				.andExpect(jsonPath("$.data.flightSegmentCount").value(2))
				.andReturn();

		String scheduleUuid = responseData(saveResult).path("scheduleUuid").asText();

		// 시나리오 13: 저장된 일정, Items, 항공편과 Segment를 하나의 상세 API로 조회
		mockMvc.perform(get("/api/v1/schedules/{scheduleUuid}", scheduleUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.schedule.title").value("Tokyo AI trip"))
				.andExpect(jsonPath("$.data.items.length()").value(3))
				.andExpect(jsonPath("$.data.flight.tripType").value("ROUND_TRIP"))
				.andExpect(jsonPath("$.data.flight.outbound[0].flightNumber").value("701"))
				.andExpect(jsonPath("$.data.flight['return'][0].flightNumber").value("801"));
	}

	@Test
	void generatesAiScheduleWithoutFlightSearchCondition() throws Exception {
		when(aiScheduleGenerationPort.generate(any())).thenReturn(generatedSchedule());

		// 시나리오 1: 항공편 없이 AI 일정 생성
		mockMvc.perform(post("/api/v1/schedules/ai/generate")
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(aiGenerateRequest(false)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items.length()").value(3));

		ArgumentCaptor<AiScheduleGenerateCommand> commandCaptor =
				ArgumentCaptor.forClass(AiScheduleGenerateCommand.class);
		verify(aiScheduleGenerationPort).generate(commandCaptor.capture());
		assertThat(commandCaptor.getValue().flight()).isNull();
	}

	@Test
	void returnsEmptyCandidateListsWhenNoFlightMatches() throws Exception {
		when(flightSearchPort.search(any())).thenReturn(List.of());

		// 시나리오 5: 항공편 검색 결과 없음
		mockMvc.perform(post("/api/v1/flights/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(flightSearchRequest()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.outboundCandidates").isEmpty())
				.andExpect(jsonPath("$.data.returnCandidates").isEmpty());
	}

	@Test
	void returnsBadGatewayWhenAviationStackFails() throws Exception {
		when(flightSearchPort.search(any()))
				.thenThrow(new FlightSearchException("AviationStack unavailable", "provider_error", null));

		// 시나리오 10: Aviationstack API 오류
		mockMvc.perform(post("/api/v1/flights/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(flightSearchRequest()))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.error.code").value("FLIGHT_SEARCH_FAILED"));
	}

	@Test
	void returnsStableFailureResponseWhenAviationStackRequestLimitIsExceeded() throws Exception {
		when(flightSearchPort.search(any()))
				.thenThrow(new FlightSearchException("Monthly request limit exceeded", "usage_limit_reached", null));

		// 시나리오 11: Aviationstack API 요청 한도 초과
		mockMvc.perform(post("/api/v1/flights/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(flightSearchRequest()))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.error.code").value("FLIGHT_SEARCH_FAILED"));
	}

	@Test
	void savesAndReturnsScheduleWithoutFlight() throws Exception {
		// 시나리오 12: 항공편 없는 일정 상세 조회
		MvcResult saveResult = mockMvc.perform(post("/api/v1/schedules/ai/save")
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(saveRequestWithoutFlight()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.flightSaved").value(false))
				.andReturn();

		String scheduleUuid = responseData(saveResult).path("scheduleUuid").asText();
		mockMvc.perform(get("/api/v1/schedules/{scheduleUuid}", scheduleUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items.length()").value(3))
				.andExpect(jsonPath("$.data.flight").doesNotExist());
	}

	@Test
	void savesStopoverCandidatesAsOrderedSegments() throws Exception {
		// 시나리오 9: 경유편을 순서가 보장된 Segment로 저장
		MvcResult saveResult = mockMvc.perform(post("/api/v1/schedules/ai/save")
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(saveRequestWithStopover()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.flightSegmentCount").value(3))
				.andReturn();

		String scheduleUuid = responseData(saveResult).path("scheduleUuid").asText();
		mockMvc.perform(get("/api/v1/schedules/{scheduleUuid}", scheduleUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.flight.outbound.length()").value(2))
				.andExpect(jsonPath("$.data.flight.outbound[0].segmentOrder").value(1))
				.andExpect(jsonPath("$.data.flight.outbound[0].arrivalAirportCode").value("KIX"))
				.andExpect(jsonPath("$.data.flight.outbound[1].segmentOrder").value(2))
				.andExpect(jsonPath("$.data.flight.outbound[1].departureAirportCode").value("KIX"))
				.andExpect(jsonPath("$.data.flight.outbound[1].arrivalAirportCode").value("NRT"));
	}

	private GeneratedAiSchedule generatedSchedule() {
		return new GeneratedAiSchedule(
				"Tokyo AI trip",
				"Three-day Tokyo itinerary",
				List.of(
						generatedItem(1, "Tokyo arrival"),
						generatedItem(2, "Tokyo tour"),
						generatedItem(3, "Return home")
				)
		);
	}

	private GeneratedScheduleItem generatedItem(int day, String subtitle) {
		return new GeneratedScheduleItem(
				day, LocalTime.of(10, 0), subtitle, ScheduleItemType.TOUR, subtitle + " description",
				0L, null, null, null, null
		);
	}

	private List<FlightCandidate> candidatesFor(FlightSearchCriteria criteria) {
		if ("ICN".equals(criteria.departureAirportCode()) && "NRT".equals(criteria.arrivalAirportCode())) {
			return List.of(
					candidate("ICN", "NRT", "2026-09-01T09:00:00+09:00", "2026-09-01T11:30:00+09:00", "701"),
					candidate("ICN", "NRT", "2026-09-01T10:00:00+09:00", "2026-09-01T12:40:00+09:00", "702"),
					candidate("ICN", "NRT", "2026-09-01T11:00:00+09:00", "2026-09-01T13:50:00+09:00", "703"),
					candidate("ICN", "NRT", "2026-09-01T12:00:00+09:00", "2026-09-01T15:00:00+09:00", "704")
			);
		}
		if ("NRT".equals(criteria.departureAirportCode()) && "ICN".equals(criteria.arrivalAirportCode())) {
			return List.of(
					candidate("NRT", "ICN", "2026-09-03T18:00:00+09:00", "2026-09-03T20:30:00+09:00", "801"),
					candidate("NRT", "ICN", "2026-09-03T19:00:00+09:00", "2026-09-03T21:40:00+09:00", "802"),
					candidate("NRT", "ICN", "2026-09-03T20:00:00+09:00", "2026-09-03T22:50:00+09:00", "803")
			);
		}
		return List.of();
	}

	private FlightCandidate candidate(
			String departureCode,
			String arrivalCode,
			String departureAt,
			String arrivalAt,
			String flightNumber
	) {
		OffsetDateTime departureTime = OffsetDateTime.parse(departureAt);
		OffsetDateTime arrivalTime = OffsetDateTime.parse(arrivalAt);
		return new FlightCandidate(
				departureTime.toLocalDate(), "scheduled",
				new AirportSchedule(departureCode, "1", "10", departureTime, timezone(departureCode)),
				new AirportSchedule(arrivalCode, "2", "20", arrivalTime, timezone(arrivalCode)),
				"KE", flightNumber, "KE", "B789",
				java.time.Duration.between(departureTime, arrivalTime).toMinutes()
		);
	}

	private String aiGenerateRequest(boolean includeFlight) {
		String flight = includeFlight ? """
				,
				  "flight": {
				    "departureLocation": "서울",
				    "originLocationCode": "ICN",
				    "destinationLocationCode": "NRT",
				    "tripType": "ROUND_TRIP"
				  }
				""" : "";
		return """
				{
				  "destination": "도쿄",
				  "startDate": "2026-09-01",
				  "endDate": "2026-09-03",
				  "participantCount": 2,
				  "estimatedBudget": 800000,
				  "transportation": "TRAIN_PUBLIC_TRANSIT",
				  "travelStyle": "TOUR_LANDMARK"%s
				}
				""".formatted(flight);
	}

	private String flightSearchRequest() {
		return """
				{
				  "departureAirportCode": "ICN",
				  "arrivalAirportCode": "NRT",
				  "departureDate": "2026-09-01",
				  "returnDate": "2026-09-03",
				  "tripType": "ROUND_TRIP"
				}
				""";
	}

	private String confirmationRequest() {
		return """
				{
				  "tripType": "ROUND_TRIP",
				  "outboundCandidate": %s,
				  "returnCandidate": %s,
				  "refreshLatestInformation": false
				}
				""".formatted(
				candidateJson("2026-09-01", "ICN", "NRT", "2026-09-01T09:00:00+09:00",
						"2026-09-01T11:30:00+09:00", "701"),
				candidateJson("2026-09-03", "NRT", "ICN", "2026-09-03T18:00:00+09:00",
						"2026-09-03T20:30:00+09:00", "801")
		);
	}

	private String saveRequestWithDirectRoundTrip() {
		return saveRequest("""
				"flight": {
				  "departureLocation": "서울",
				  "tripType": "ROUND_TRIP",
				  "outboundCandidates": [%s],
				  "returnCandidates": [%s]
				}
				""".formatted(
				candidateJson("2026-09-01", "ICN", "NRT", "2026-09-01T09:00:00+09:00",
						"2026-09-01T11:30:00+09:00", "701"),
				candidateJson("2026-09-03", "NRT", "ICN", "2026-09-03T18:00:00+09:00",
						"2026-09-03T20:30:00+09:00", "801")
		));
	}

	private String saveRequestWithStopover() {
		return saveRequest("""
				"flight": {
				  "departureLocation": "서울",
				  "tripType": "ROUND_TRIP",
				  "outboundCandidates": [%s, %s],
				  "returnCandidates": [%s]
				}
				""".formatted(
				candidateJson("2026-09-01", "ICN", "KIX", "2026-09-01T08:00:00+09:00",
						"2026-09-01T10:00:00+09:00", "601"),
				candidateJson("2026-09-01", "KIX", "NRT", "2026-09-01T11:00:00+09:00",
						"2026-09-01T12:30:00+09:00", "602"),
				candidateJson("2026-09-03", "NRT", "ICN", "2026-09-03T18:00:00+09:00",
						"2026-09-03T20:30:00+09:00", "603")
		));
	}

	private String saveRequestWithoutFlight() {
		return saveRequest(null);
	}

	private String saveRequest(String flightField) {
		String optionalFlight = flightField == null ? "" : ",\n" + flightField;
		return """
				{
				  "title": "Tokyo AI trip",
				  "destination": "도쿄",
				  "startDate": "2026-09-01",
				  "endDate": "2026-09-03",
				  "participantCount": 2,
				  "estimatedBudget": 800000,
				  "transportation": "TRAIN_PUBLIC_TRANSIT",
				  "travelStyle": "TOUR_LANDMARK",
				  "content": "Three-day Tokyo itinerary",
				  "calendarColor": "#4F46E5",
				  "items": [
				    %s,
				    %s,
				    %s
				  ]%s
				}
				""".formatted(
				itemJson(1, "Tokyo arrival"), itemJson(2, "Tokyo tour"), itemJson(3, "Return home"),
				optionalFlight
		);
	}

	private String itemJson(int dayNumber, String subtitle) {
		return """
				{
				  "dayNumber": %d,
				  "scheduleTime": "10:00:00",
				  "subtitle": "%s",
				  "scheduleType": "TOUR",
				  "description": "%s description",
				  "estimatedCost": 0
				}
				""".formatted(dayNumber, subtitle, subtitle).trim();
	}

	private String candidateJson(
			String flightDate,
			String departureCode,
			String arrivalCode,
			String departureAt,
			String arrivalAt,
			String flightNumber
	) {
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
				flightDate, departureCode, departureAt, timezone(departureCode),
				arrivalCode, arrivalAt, timezone(arrivalCode), flightNumber
		).trim();
	}

	private String timezone(String airportCode) {
		return switch (airportCode) {
			case "ICN" -> "Asia/Seoul";
			case "NRT" -> "Asia/Tokyo";
			case "KIX" -> "Asia/Tokyo";
			default -> "UTC";
		};
	}

	private JsonNode responseData(MvcResult result) throws Exception {
		return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
	}
}
