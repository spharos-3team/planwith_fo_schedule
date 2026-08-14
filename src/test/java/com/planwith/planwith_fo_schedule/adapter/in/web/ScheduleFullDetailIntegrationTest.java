package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.FlightDirection;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.ScheduleFlight;
import com.planwith.planwith_fo_schedule.domain.ScheduleFlightSegment;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.TripType;
import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScheduleFullDetailIntegrationTest {

	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private ScheduleRepositoryPort scheduleRepositoryPort;

	@Autowired
	private EntityManager entityManager;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
	}

	@Test
	void returnsScheduleItemsOutboundAndReturnSegmentsInOneResponse() throws Exception {
		Schedule saved = scheduleRepositoryPort.save(createSchedule().withFlight(createFlight()));
		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(get("/api/v1/schedules/{scheduleUuid}", saved.scheduleUuid().value()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.schedule.scheduleUuid")
						.value(saved.scheduleUuid().value().toString()))
				.andExpect(jsonPath("$.data.schedule.title").value("Tokyo AI trip"))
				.andExpect(jsonPath("$.data.schedule.destination").value("Tokyo"))
				.andExpect(jsonPath("$.data.items.length()").value(3))
				.andExpect(jsonPath("$.data.items[0].dayNumber").value(1))
				.andExpect(jsonPath("$.data.items[0].subtitle").value("Tokyo arrival"))
				.andExpect(jsonPath("$.data.flight.provider").value("AVIATIONSTACK"))
				.andExpect(jsonPath("$.data.flight.originLocationCode").value("ICN"))
				.andExpect(jsonPath("$.data.flight.destinationLocationCode").value("NRT"))
				.andExpect(jsonPath("$.data.flight.tripType").value("ROUND_TRIP"))
				.andExpect(jsonPath("$.data.flight.outbound.length()").value(2))
				.andExpect(jsonPath("$.data.flight.outbound[0].segmentOrder").value(1))
				.andExpect(jsonPath("$.data.flight.outbound[0].departureAirportCode").value("ICN"))
				.andExpect(jsonPath("$.data.flight.outbound[0].arrivalAirportCode").value("KIX"))
				.andExpect(jsonPath("$.data.flight.outbound[1].segmentOrder").value(2))
				.andExpect(jsonPath("$.data.flight.outbound[1].arrivalAirportCode").value("NRT"))
				.andExpect(jsonPath("$.data.flight['return'].length()").value(1))
				.andExpect(jsonPath("$.data.flight['return'][0].flightNumber").value("704"))
				.andExpect(jsonPath("$.data.flight['return'][0].departureTerminal").value("2"))
				.andExpect(jsonPath("$.data.flight['return'][0].departureGate").value("30"))
				.andExpect(jsonPath("$.data.flight['return'][0].flightStatus").value("scheduled"))
				.andExpect(jsonPath("$.data.flight['return'][0].durationMinutes").value(150));
	}

	private Schedule createSchedule() {
		return Schedule.create(
				new MemberUuid(UUID.randomUUID()), "Tokyo AI trip", "Tokyo",
				LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3), new Headcount(2),
				ScheduleCost.of(800_000L), TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.TOUR_LANDMARK, "Tokyo itinerary", "#4F46E5",
				ScheduleCreatorType.AI,
				List.of(item(1, "Tokyo arrival"), item(2, "Tokyo tour"), item(3, "Return home"))
		);
	}

	private ScheduleItem item(int day, String title) {
		return ScheduleItem.create(
				new DayNumber(day), ScheduleItemType.TOUR, title, title + " description", null,
				LocalTime.of(10, 0), ScheduleCost.zero()
		);
	}

	private ScheduleFlight createFlight() {
		ScheduleFlightSegment outboundFirst = segment(
				FlightDirection.OUTBOUND, 1, "ICN", "KIX", "1", "10",
				"2026-09-01T09:00:00+09:00", "2026-09-01T11:00:00+09:00", "701", 120
		);
		ScheduleFlightSegment outboundSecond = segment(
				FlightDirection.OUTBOUND, 2, "KIX", "NRT", "1", "20",
				"2026-09-01T12:00:00+09:00", "2026-09-01T13:30:00+09:00", "703", 90
		);
		ScheduleFlightSegment inbound = segment(
				FlightDirection.RETURN, 1, "NRT", "ICN", "2", "30",
				"2026-09-03T18:00:00+09:00", "2026-09-03T20:30:00+09:00", "704", 150
		);
		return ScheduleFlight.create(
				"AVIATIONSTACK", "Seoul", "ICN", "Tokyo", "NRT", TripType.ROUND_TRIP,
				List.of(outboundFirst, outboundSecond, inbound)
		);
	}

	private ScheduleFlightSegment segment(
			FlightDirection direction,
			int order,
			String departure,
			String arrival,
			String terminal,
			String gate,
			String departureAt,
			String arrivalAt,
			String flightNumber,
			int duration
	) {
		return ScheduleFlightSegment.create(
				direction, order, departure, arrival, terminal, terminal, gate, gate,
				OffsetDateTime.parse(departureAt), OffsetDateTime.parse(arrivalAt),
				"Asia/Tokyo", "Asia/Tokyo", "KE", flightNumber, "KE", "B789", "scheduled", duration
		);
	}
}
