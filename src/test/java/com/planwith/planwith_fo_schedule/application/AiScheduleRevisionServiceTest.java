package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.exception.AiScheduleGenerationException;
import com.planwith.planwith_fo_schedule.application.exception.ScheduleAccessDeniedException;
import com.planwith.planwith_fo_schedule.application.exception.ScheduleNotFoundException;
import com.planwith.planwith_fo_schedule.application.model.OpenAiUsage;
import com.planwith.planwith_fo_schedule.application.port.in.ReviseScheduleWithAiUseCase.ReviseScheduleCommand;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleRevisionPort;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleRevisionPort.RevisedSchedule;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleRevisionPort.ScheduleRevisionContext;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

class AiScheduleRevisionServiceTest {

	private ScheduleRepositoryPort repository;
	private AiScheduleRevisionPort revisionPort;
	private AiScheduleRevisionService service;

	@BeforeEach
	void setUp() {
		repository = mock(ScheduleRepositoryPort.class);
		revisionPort = mock(AiScheduleRevisionPort.class);
		service = new AiScheduleRevisionService(repository, revisionPort);
	}

	@Test
	void returnsRevisionDraftWithoutPersistingSchedule() {
		Schedule schedule = createSchedule(UUID.randomUUID());
		when(repository.findByScheduleUuid(schedule.scheduleUuid())).thenReturn(Optional.of(schedule));
		when(revisionPort.revise(any())).thenReturn(
				new RevisedSchedule(
						"첫날에는 해운대를 여유롭게 산책합니다.",
						new OpenAiUsage("gpt-4o-mini-2024-07-18", 90, 30, 120)
				)
		);

		var result = service.revise(new ReviseScheduleCommand(
				schedule.memberUuid().value(),
				schedule.scheduleUuid().value(),
				"바다 중심으로 더 자세히 작성해줘"
		));

		assertThat(result.scheduleUuid()).isEqualTo(schedule.scheduleUuid().value());
		assertThat(result.revisedContent()).contains("해운대");
		assertThat(result.usage().model()).isEqualTo("gpt-4o-mini-2024-07-18");
		assertThat(result.usage().totalTokens()).isEqualTo(120);
		verify(revisionPort).revise(any(ScheduleRevisionContext.class));
		verify(repository, never()).update(any());
	}

	@Test
	void rejectsRevisionWhenScheduleDoesNotExist() {
		UUID scheduleUuid = UUID.randomUUID();
		when(repository.findByScheduleUuid(new ScheduleUuid(scheduleUuid))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.revise(new ReviseScheduleCommand(
				UUID.randomUUID(),
				scheduleUuid,
				"일정을 여유롭게 바꿔줘"
		))).isInstanceOf(ScheduleNotFoundException.class);

		verifyNoInteractions(revisionPort);
	}

	@Test
	void rejectsRevisionRequestedByAnotherMember() {
		Schedule schedule = createSchedule(UUID.randomUUID());
		when(repository.findByScheduleUuid(schedule.scheduleUuid())).thenReturn(Optional.of(schedule));

		assertThatThrownBy(() -> service.revise(new ReviseScheduleCommand(
				UUID.randomUUID(),
				schedule.scheduleUuid().value(),
				"일정을 여유롭게 바꿔줘"
		))).isInstanceOf(ScheduleAccessDeniedException.class);

		verifyNoInteractions(revisionPort);
		verify(repository, never()).update(any());
	}

	@Test
	void rejectsBlankRevisionReturnedByAi() {
		Schedule schedule = createSchedule(UUID.randomUUID());
		when(repository.findByScheduleUuid(schedule.scheduleUuid())).thenReturn(Optional.of(schedule));
		when(revisionPort.revise(any())).thenReturn(new RevisedSchedule(" "));

		assertThatThrownBy(() -> service.revise(new ReviseScheduleCommand(
				schedule.memberUuid().value(),
				schedule.scheduleUuid().value(),
				"일정을 여유롭게 바꿔줘"
		))).isInstanceOf(AiScheduleGenerationException.class);

		verify(repository, never()).update(any());
	}

	private Schedule createSchedule(UUID memberUuid) {
		return Schedule.create(
				new MemberUuid(memberUuid),
				"부산 여행",
				"부산",
				LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 9, 3),
				new Headcount(2),
				ScheduleCost.of(500_000),
				TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.TOUR_LANDMARK,
				"해운대와 광안리를 방문합니다.",
				"#3366FF",
				ScheduleCreatorType.USER,
				List.of()
		);
	}
}
