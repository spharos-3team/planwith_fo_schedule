package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.port.in.SaveAiScheduleUseCase.SaveAiScheduleCommand;
import com.planwith.planwith_fo_schedule.application.port.in.SaveAiScheduleUseCase.SaveAiScheduleItemCommand;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

class SaveAiScheduleServiceTest {

	@Test
	void savesConfirmedDraftWithAiCreatorTypeUsingScheduleAggregate() {
		CapturingScheduleRepository repository = new CapturingScheduleRepository();
		SaveAiScheduleService service = new SaveAiScheduleService(repository);
		UUID memberUuid = UUID.randomUUID();

		var result = service.save(new SaveAiScheduleCommand(
				memberUuid,
				"부산 AI 여행",
				"부산",
				LocalDate.of(2026, 8, 20),
				LocalDate.of(2026, 8, 20),
				2,
				500_000L,
				TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.TOUR_LANDMARK,
				null,
				"#4F46E5",
				List.of(new SaveAiScheduleItemCommand(
						1,
						LocalTime.of(10, 0),
						"해운대 산책",
						ScheduleItemType.TOUR,
						"해변을 산책합니다.",
						0L,
						"해운대",
						"부산광역시 해운대구",
						new BigDecimal("35.1587000"),
						new BigDecimal("129.1604000")
				))
		));

		Schedule saved = repository.savedSchedule;
		assertThat(saved).isNotNull();
		assertThat(saved.memberUuid().value()).isEqualTo(memberUuid);
		assertThat(saved.creatorType()).isEqualTo(ScheduleCreatorType.AI);
		assertThat(saved.items()).hasSize(1);
		assertThat(saved.items().get(0).itemType()).isEqualTo(ScheduleItemType.TOUR);
		assertThat(result.scheduleUuid()).isEqualTo(saved.scheduleUuid().value());
		assertThat(result.itemCount()).isEqualTo(1);
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
			throw new UnsupportedOperationException();
		}

		@Override
		public Schedule softDelete(Schedule schedule) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<Schedule> findByScheduleUuid(ScheduleUuid scheduleUuid) {
			return Optional.empty();
		}
	}
}
