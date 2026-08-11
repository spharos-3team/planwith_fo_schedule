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
import com.planwith.planwith_fo_schedule.application.port.in.UpdateScheduleUseCase.UpdateScheduleCommand;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.CreatorType;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

class UpdateScheduleServiceTest {

	@Test
	void updatesAndSavesExistingSchedule() {
		ScheduleRepositoryPort repository = mock(ScheduleRepositoryPort.class);
		UpdateScheduleService service = new UpdateScheduleService(repository);
		Schedule existingSchedule = createSchedule();
		when(repository.findByScheduleUuid(existingSchedule.scheduleUuid()))
				.thenReturn(Optional.of(existingSchedule));
		when(repository.update(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = service.updateSchedule(
				existingSchedule.scheduleUuid().value(),
				new UpdateScheduleCommand(
						"제주 휴가",
						"제주",
						null,
						LocalDate.of(2026, 9, 4),
						3,
						700_000L,
						"렌터카",
						"가족 자유여행",
						"#22AA88"
				)
		);

		ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
		verify(repository).update(scheduleCaptor.capture());
		Schedule updatedSchedule = scheduleCaptor.getValue();
		assertThat(updatedSchedule.memberUuid()).isEqualTo(existingSchedule.memberUuid());
		assertThat(updatedSchedule.scheduleUuid()).isEqualTo(existingSchedule.scheduleUuid());
		assertThat(updatedSchedule.creatorType()).isEqualTo(CreatorType.SELF);
		assertThat(updatedSchedule.destination()).isEqualTo("제주");
		assertThat(updatedSchedule.period().startDate()).isEqualTo(LocalDate.of(2026, 9, 1));
		assertThat(updatedSchedule.period().endDate()).isEqualTo(LocalDate.of(2026, 9, 4));
		assertThat(result.title()).isEqualTo("제주 휴가");
		assertThat(result.headcount()).isEqualTo(3);
		assertThat(result.expectedCost()).isEqualTo(700_000L);
	}

	@Test
	void throwsNotFoundWithoutSaving() {
		ScheduleRepositoryPort repository = mock(ScheduleRepositoryPort.class);
		UpdateScheduleService service = new UpdateScheduleService(repository);
		UUID scheduleUuid = UUID.randomUUID();
		when(repository.findByScheduleUuid(new ScheduleUuid(scheduleUuid))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateSchedule(
				scheduleUuid,
				new UpdateScheduleCommand(null, null, null, null, null, null, null, null, null)
		)).isInstanceOf(ScheduleNotFoundException.class);

		verify(repository, never()).update(any());
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
				"KTX",
				"해운대 방문",
				"#3366FF",
				CreatorType.SELF,
				List.of()
		);
	}
}
