package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import com.planwith.planwith_fo_schedule.domain.FlightTravelClass;
import com.planwith.planwith_fo_schedule.domain.FlightTripType;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
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
				"대중교통",
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
		ScheduleJpaEntity schedule = new ScheduleJpaEntity(
				null,
				UUID.randomUUID(),
				UUID.randomUUID(),
				"도쿄 여행",
				"도쿄",
				LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 9, 5),
				2,
				1_500_000L,
				"항공",
				null,
				"#3366FF",
				ScheduleCreatorType.AI,
				null,
				null,
				null
		);
		ScheduleFlightJpaEntity flight = new ScheduleFlightJpaEntity(
				null,
				"AMADEUS",
				"인천",
				"ICN",
				"도쿄",
				"NRT",
				FlightTripType.ROUND_TRIP,
				FlightTravelClass.ECONOMY,
				new BigDecimal("420000.00"),
				"KRW",
				LocalDateTime.of(2026, 8, 11, 13, 0),
				null,
				null
		);
		ScheduleFlightSegmentJpaEntity segment = new ScheduleFlightSegmentJpaEntity(
				null,
				FlightDirection.OUTBOUND,
				1,
				"ICN",
				"NRT",
				"1",
				"2",
				LocalDateTime.of(2026, 9, 1, 9, 0),
				LocalDateTime.of(2026, 9, 1, 11, 30),
				"KE",
				"703",
				"KE",
				"789",
				150,
				null
		);
		flight.addSegment(segment);
		schedule.assignFlight(flight);

		ScheduleJpaEntity savedSchedule = springDataScheduleRepository.saveAndFlush(schedule);

		assertThat(savedSchedule.getFlight().getScheduleFlightId()).isNotNull();
		assertThat(savedSchedule.getFlight().getScheduleId()).isEqualTo(savedSchedule.getScheduleId());
		assertThat(savedSchedule.getFlight().getProvider()).isEqualTo("AMADEUS");
		assertThat(savedSchedule.getFlight().getTripType()).isEqualTo(FlightTripType.ROUND_TRIP);
		assertThat(savedSchedule.getFlight().getTravelClass()).isEqualTo(FlightTravelClass.ECONOMY);
		assertThat(savedSchedule.getFlight().getSegments()).singleElement().satisfies(savedSegment -> {
			assertThat(savedSegment.getScheduleFlightSegmentId()).isNotNull();
			assertThat(savedSegment.getScheduleFlightId()).isEqualTo(savedSchedule.getFlight().getScheduleFlightId());
			assertThat(savedSegment.getDirection()).isEqualTo(FlightDirection.OUTBOUND);
			assertThat(savedSegment.getSegmentOrder()).isEqualTo(1);
		});
	}
}
