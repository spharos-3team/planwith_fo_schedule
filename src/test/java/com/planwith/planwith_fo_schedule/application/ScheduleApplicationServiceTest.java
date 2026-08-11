package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase.CreateScheduleCommand;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.CreatorType;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

class ScheduleApplicationServiceTest {

	@Test
	void createsAndSavesSelfScheduleWithoutDetailItems() {
		CapturingScheduleRepository repository = new CapturingScheduleRepository();
		ScheduleApplicationService service = new ScheduleApplicationService(repository);
		UUID memberUuid = UUID.randomUUID();

		var result = service.createSchedule(new CreateScheduleCommand(
				memberUuid,
				null,
				"서울",
				LocalDate.of(2026, 8, 10),
				LocalDate.of(2026, 8, 12),
				2,
				null,
				"대중교통",
				"경복궁 관람 후 한강을 산책한다.",
				null
		));

		assertThat(repository.savedSchedule).isNotNull();
		assertThat(repository.savedSchedule.memberUuid().value()).isEqualTo(memberUuid);
		assertThat(repository.savedSchedule.title()).isEqualTo("서울 여행");
		assertThat(repository.savedSchedule.calendarColor()).isEqualTo(Schedule.DEFAULT_CALENDAR_COLOR);
		assertThat(repository.savedSchedule.headcount().value()).isEqualTo(2);
		assertThat(repository.savedSchedule.expectedCost().amount()).isNull();
		assertThat(repository.savedSchedule.creatorType()).isEqualTo(CreatorType.SELF);
		assertThat(repository.savedSchedule.content()).isEqualTo("경복궁 관람 후 한강을 산책한다.");
		assertThat(repository.savedSchedule.items()).isEmpty();
		assertThat(result.scheduleUuid()).isEqualTo(repository.savedSchedule.scheduleUuid().value());
	}

	private static final class CapturingScheduleRepository implements ScheduleRepositoryPort {
		private Schedule savedSchedule;

		@Override
		public Schedule save(Schedule schedule) {
			this.savedSchedule = schedule;
			return schedule;
		}

		@Override
		public Schedule update(Schedule schedule) {
			throw new UnsupportedOperationException("Update is not used by the creation service test.");
		}

		@Override
		public Schedule softDelete(Schedule schedule) {
			throw new UnsupportedOperationException("Delete is not used by the creation service test.");
		}

		@Override
		public Optional<Schedule> findByScheduleUuid(ScheduleUuid scheduleUuid) {
			return Optional.ofNullable(savedSchedule)
					.filter(schedule -> schedule.scheduleUuid().equals(scheduleUuid));
		}
	}
}
