package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.planwith.planwith_fo_schedule.application.exception.ScheduleNotFoundException;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

class DeleteScheduleServiceTest {

	@Test
	void marksExistingScheduleAsDeleted() {
		ScheduleRepositoryPort repository = mock(ScheduleRepositoryPort.class);
		DeleteScheduleService service = new DeleteScheduleService(repository);
		Schedule schedule = createSchedule();
		when(repository.findByScheduleUuid(schedule.scheduleUuid())).thenReturn(Optional.of(schedule));
		when(repository.softDelete(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

		service.deleteSchedule(schedule.scheduleUuid().value());

		ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
		verify(repository).softDelete(captor.capture());
		assertThat(captor.getValue().deletedAt()).isNotNull();
		assertThat(captor.getValue().scheduleUuid()).isEqualTo(schedule.scheduleUuid());
		assertThat(captor.getValue().memberUuid()).isEqualTo(schedule.memberUuid());
	}

	@Test
	void throwsNotFoundWithoutDeleting() {
		ScheduleRepositoryPort repository = mock(ScheduleRepositoryPort.class);
		DeleteScheduleService service = new DeleteScheduleService(repository);
		UUID scheduleUuid = UUID.randomUUID();
		when(repository.findByScheduleUuid(new ScheduleUuid(scheduleUuid))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.deleteSchedule(scheduleUuid))
				.isInstanceOf(ScheduleNotFoundException.class);
		verify(repository, never()).softDelete(any());
	}

	private Schedule createSchedule() {
		return Schedule.create(
				new MemberUuid(UUID.randomUUID()),
				"부산 여행",
				"부산",
				LocalDate.of(2026, 8, 20),
				LocalDate.of(2026, 8, 22),
				new Headcount(2),
				ScheduleCost.of(500_000L),
				TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.TOUR_LANDMARK,
				"자유여행",
				"#3366FF",
				ScheduleCreatorType.USER,
				List.of()
		);
	}
}
