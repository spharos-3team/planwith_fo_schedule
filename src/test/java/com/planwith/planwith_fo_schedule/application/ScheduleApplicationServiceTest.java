package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase.CreateScheduleCommand;
import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase.CreateScheduleItemCommand;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.CreatorType;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleType;

class ScheduleApplicationServiceTest {

	@Test
	void createsAndSavesSqlAlignedScheduleAggregate() {
		CapturingScheduleRepository repository = new CapturingScheduleRepository();
		ScheduleApplicationService service = new ScheduleApplicationService(repository);
		UUID memberUuid = UUID.randomUUID();

		var result = service.createSchedule(new CreateScheduleCommand(
				memberUuid,
				null,
				"서울",
				LocalDate.of(2026, 8, 10),
				LocalDate.of(2026, 8, 12),
				null,
				null,
				"대중교통",
				"여름 휴가",
				null,
				CreatorType.AI,
				List.of(new CreateScheduleItemCommand(
						1,
						LocalTime.of(10, 30),
						"경복궁 관람",
						ScheduleType.TOUR,
						"경복궁을 관람합니다.",
						null,
						"경복궁",
						"서울특별시 종로구 사직로 161",
						new BigDecimal("37.5796170"),
						new BigDecimal("126.9770410")
				))
		));

		assertThat(repository.savedSchedule).isNotNull();
		assertThat(repository.savedSchedule.memberUuid().value()).isEqualTo(memberUuid);
		assertThat(repository.savedSchedule.title()).isEqualTo("서울 여행");
		assertThat(repository.savedSchedule.calendarColor()).isEqualTo(Schedule.DEFAULT_CALENDAR_COLOR);
		assertThat(repository.savedSchedule.headcount().value()).isEqualTo(1);
		assertThat(repository.savedSchedule.expectedCost().amount()).isNull();
		assertThat(repository.savedSchedule.items().get(0).estimatedCost().amount()).isZero();
		assertThat(result.scheduleUuid()).isEqualTo(repository.savedSchedule.scheduleUuid().value());
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
