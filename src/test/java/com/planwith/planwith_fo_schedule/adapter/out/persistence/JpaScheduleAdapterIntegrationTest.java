package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.CreatorType;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;
import com.planwith.planwith_fo_schedule.domain.ScheduleType;
import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.GeoPoint;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;

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
				CreatorType.SELF,
				List.of(ScheduleItem.create(
						new DayNumber(1),
						LocalTime.of(10, 30),
						"경복궁 관람",
						ScheduleType.TOUR,
						"경복궁을 관람합니다.",
						ScheduleCost.of(3_000L),
						"경복궁",
						"서울특별시 종로구 사직로 161",
						new GeoPoint(new BigDecimal("37.5796170"), new BigDecimal("126.9770410"))
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
			assertThat(item.scheduleType()).isEqualTo(ScheduleType.TOUR);
			assertThat(item.location()).isNotNull();
		});
		assertThat(springDataScheduleRepository.findById(savedSchedule.scheduleId())).isPresent();
	}
}
