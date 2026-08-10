package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JpaScheduleAdapterIntegrationTest {

	@Autowired
	private ScheduleRepositoryPort scheduleRepositoryPort;

	@Autowired
	private SpringDataScheduleRepository springDataScheduleRepository;

	@Test
	void savesScheduleAndItemsAsOneAggregate() {
		Schedule schedule = Schedule.create(
				UUID.randomUUID(),
				"프로젝트 일정",
				List.of(ScheduleItem.create(
						"팀 회의",
						Instant.parse("2026-08-10T01:00:00Z"),
						Instant.parse("2026-08-10T02:00:00Z")
				))
		);

		Schedule savedSchedule = scheduleRepositoryPort.save(schedule);

		assertThat(springDataScheduleRepository.findById(savedSchedule.id()))
				.isPresent()
				.get()
				.extracting(entity -> entity.getItems().size())
				.isEqualTo(1);
	}
}
