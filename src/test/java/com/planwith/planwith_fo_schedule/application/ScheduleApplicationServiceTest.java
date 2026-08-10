package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase.CreateScheduleCommand;
import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase.CreateScheduleItemCommand;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;

class ScheduleApplicationServiceTest {

	@Test
	void createsAndSavesScheduleAggregate() {
		CapturingScheduleRepository repository = new CapturingScheduleRepository();
		ScheduleApplicationService service = new ScheduleApplicationService(repository);
		UUID ownerId = UUID.randomUUID();

		var result = service.createSchedule(new CreateScheduleCommand(
				ownerId,
				"프로젝트 일정",
				List.of(new CreateScheduleItemCommand(
						"팀 회의",
						Instant.parse("2026-08-10T01:00:00Z"),
						Instant.parse("2026-08-10T02:00:00Z")
				))
		));

		assertThat(repository.savedSchedule).isNotNull();
		assertThat(repository.savedSchedule.ownerId()).isEqualTo(ownerId);
		assertThat(result.scheduleId()).isEqualTo(repository.savedSchedule.id());
		assertThat(result.itemCount()).isEqualTo(1);
	}

	private static final class CapturingScheduleRepository implements ScheduleRepositoryPort {
		private Schedule savedSchedule;

		@Override
		public Schedule save(Schedule schedule) {
			this.savedSchedule = schedule;
			return schedule;
		}
	}
}
