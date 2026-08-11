package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.exception.ScheduleNotFoundException;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

class GetScheduleDetailServiceTest {

	@Test
	void returnsSingleScheduleDetail() {
		ScheduleRepositoryPort repository = mock(ScheduleRepositoryPort.class);
		GetScheduleDetailService service = new GetScheduleDetailService(repository);
		Schedule schedule = createSchedule();
		when(repository.findByScheduleUuid(schedule.scheduleUuid())).thenReturn(Optional.of(schedule));

		var result = service.getScheduleDetail(schedule.scheduleUuid().value());

		assertThat(result.scheduleUuid()).isEqualTo(schedule.scheduleUuid().value());
		assertThat(result.title()).isEqualTo("부산 여행");
		assertThat(result.destination()).isEqualTo("부산");
		assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 9, 1));
		assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 9, 3));
		assertThat(result.headcount()).isEqualTo(2);
		assertThat(result.expectedCost()).isEqualTo(500_000L);
		assertThat(result.transportation()).isEqualTo("대중교통");
		assertThat(result.content()).isEqualTo("해운대 방문");
		assertThat(result.calendarColor()).isEqualTo("#3366FF");
		assertThat(result.creatorType()).isEqualTo(ScheduleCreatorType.USER);
		verify(repository).findByScheduleUuid(schedule.scheduleUuid());
	}

	@Test
	void throwsNotFoundWhenScheduleDoesNotExist() {
		ScheduleRepositoryPort repository = mock(ScheduleRepositoryPort.class);
		GetScheduleDetailService service = new GetScheduleDetailService(repository);
		UUID scheduleUuid = UUID.randomUUID();
		when(repository.findByScheduleUuid(new ScheduleUuid(scheduleUuid))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getScheduleDetail(scheduleUuid))
				.isInstanceOfSatisfying(
						ScheduleNotFoundException.class,
						exception -> assertThat(exception.scheduleUuid()).isEqualTo(scheduleUuid)
				);
	}

	private Schedule createSchedule() {
		return Schedule.create(
				new MemberUuid(UUID.randomUUID()),
				"부산 여행",
				"부산",
				LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 9, 3),
				new Headcount(2),
				ScheduleCost.of(500_000L),
				"대중교통",
				"해운대 방문",
				"#3366FF",
				ScheduleCreatorType.USER,
				List.of()
		);
	}
}
