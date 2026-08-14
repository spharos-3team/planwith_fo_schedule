package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.FlightDirection;
import com.planwith.planwith_fo_schedule.domain.TripType;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleFlight;
import com.planwith.planwith_fo_schedule.domain.ScheduleFlightSegment;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.GeoPoint;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleItemLocation;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JpaScheduleAdapterIntegrationTest {

	@Autowired
	private ScheduleRepositoryPort scheduleRepositoryPort;

	@Autowired
	private SpringDataScheduleRepository springDataScheduleRepository;

	@Test
	void mapsAndSavesAllScheduleAggregateFields() {
		Schedule schedule = Schedule.create(
				new MemberUuid(UUID.randomUUID()),
				"서울 여행",
				"서울",
				LocalDate.of(2026, 8, 10),
				LocalDate.of(2026, 8, 12),
				new Headcount(2),
				ScheduleCost.of(300_000L),
				TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.TOUR_LANDMARK,
				"여름 휴가",
				"#3366FF",
				ScheduleCreatorType.USER,
				List.of(ScheduleItem.create(
						new DayNumber(1),
						ScheduleItemType.TOUR,
						"경복궁 관람",
						"경복궁을 관람합니다.",
						new ScheduleItemLocation(
								"경복궁",
								"서울특별시 종로구 사직로 161",
								new GeoPoint(new BigDecimal("37.5796170"), new BigDecimal("126.9770410"))
						),
						LocalTime.of(10, 30),
						ScheduleCost.of(3_000L)
				))
		);

		Schedule savedSchedule = scheduleRepositoryPort.save(schedule);
		springDataScheduleRepository.flush();

		assertThat(savedSchedule.scheduleId()).isNotNull();
		assertThat(savedSchedule.scheduleUuid()).isEqualTo(schedule.scheduleUuid());
		assertThat(savedSchedule.createdAt()).isNotNull();
		assertThat(savedSchedule.updatedAt()).isNotNull();
		assertThat(savedSchedule.transportation()).isEqualTo(TransportationType.TRAIN_PUBLIC_TRANSIT);
		assertThat(savedSchedule.travelStyle()).isEqualTo(TravelStyle.TOUR_LANDMARK);
		assertThat(savedSchedule.items()).singleElement().satisfies(item -> {
			assertThat(item.scheduleItemId()).isNotNull();
			assertThat(item.scheduleId()).isEqualTo(savedSchedule.scheduleId());
			assertThat(item.itemType()).isEqualTo(ScheduleItemType.TOUR);
			assertThat(item.startTime()).isEqualTo(LocalTime.of(10, 30));
			assertThat(item.location()).isNotNull();
		});
		assertThat(springDataScheduleRepository.findById(savedSchedule.scheduleId())).isPresent();
		assertThat(scheduleRepositoryPort.findByScheduleUuid(savedSchedule.scheduleUuid()))
				.isPresent()
				.get()
				.extracting(Schedule::scheduleId)
				.isEqualTo(savedSchedule.scheduleId());
	}

	@Test
	void persistsEveryScheduleCreatorType() {
		for (ScheduleCreatorType creatorType : ScheduleCreatorType.values()) {
			Schedule schedule = Schedule.create(
					new MemberUuid(UUID.randomUUID()),
					creatorType + " 일정",
					"테스트 목적지",
					LocalDate.of(2026, 10, 1),
					LocalDate.of(2026, 10, 2),
					new Headcount(1),
					ScheduleCost.zero(),
					null,
					null,
					null,
					null,
					creatorType,
					List.of()
			);

			Schedule saved = scheduleRepositoryPort.save(schedule);

			assertThat(saved.creatorType()).isEqualTo(creatorType);
			assertThat(springDataScheduleRepository.findById(saved.scheduleId()))
					.isPresent()
					.get()
					.extracting(ScheduleJpaEntity::getCreatorType)
					.isEqualTo(creatorType);
		}
	}

	@Test
	void mapsAndSavesFlightWithOrderedSegments() {
		Schedule schedule = Schedule.create(
				new MemberUuid(UUID.randomUUID()),
				"도쿄 여행",
				"도쿄",
				LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 9, 5),
				new Headcount(2),
				ScheduleCost.of(1_500_000L),
				TransportationType.OTHER,
				TravelStyle.TOUR_LANDMARK,
				null,
				"#3366FF",
				ScheduleCreatorType.AI,
				List.of()
		);
		ScheduleFlightSegment outbound = ScheduleFlightSegment.create(
				FlightDirection.OUTBOUND, 1, "ICN", "NRT", "1", "2", "10", "20",
				OffsetDateTime.parse("2026-09-01T09:00:00+09:00"),
				OffsetDateTime.parse("2026-09-01T11:30:00+09:00"),
				"Asia/Seoul", "Asia/Tokyo", "KE", "703", "KE", "789", "scheduled", 150
		);
		ScheduleFlightSegment inbound = ScheduleFlightSegment.create(
				FlightDirection.RETURN, 1, "NRT", "ICN", "2", "1", "30", "40",
				OffsetDateTime.parse("2026-09-05T18:00:00+09:00"),
				OffsetDateTime.parse("2026-09-05T20:30:00+09:00"),
				"Asia/Tokyo", "Asia/Seoul", "KE", "704", "KE", "789", "scheduled", 150
		);
		ScheduleFlight flight = ScheduleFlight.create(
				"AVIATIONSTACK",
				"인천",
				"ICN",
				"도쿄",
				"NRT",
				TripType.ROUND_TRIP,
				List.of(outbound, inbound)
		);

		Schedule savedSchedule = scheduleRepositoryPort.save(schedule.withFlight(flight));
		springDataScheduleRepository.flush();

		assertThat(savedSchedule.flight()).isNotNull();
		assertThat(savedSchedule.flight().scheduleFlightId()).isNotNull();
		assertThat(savedSchedule.flight().scheduleId()).isEqualTo(savedSchedule.scheduleId());
		assertThat(savedSchedule.flight().provider()).isEqualTo("AVIATIONSTACK");
		assertThat(savedSchedule.flight().tripType()).isEqualTo(TripType.ROUND_TRIP);
		assertThat(savedSchedule.flight().segments()).hasSize(2).allSatisfy(savedSegment -> {
			assertThat(savedSegment.scheduleFlightSegmentId()).isNotNull();
			assertThat(savedSegment.scheduleFlightId()).isEqualTo(savedSchedule.flight().scheduleFlightId());
			assertThat(savedSegment.departureGate()).isNotBlank();
			assertThat(savedSegment.departureTimezone()).isNotBlank();
			assertThat(savedSegment.flightStatus()).isEqualTo("scheduled");
		});
	}
}
