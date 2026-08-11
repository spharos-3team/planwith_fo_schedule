package com.planwith.planwith_fo_schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;

class ScheduleUpdateTest {

	@Test
	void updatesMutableFieldsAndKeepsIdentityFields() {
		Schedule schedule = createSchedule(List.of());

		Schedule updated = schedule.update(
				"제주 휴가",
				"제주",
				null,
				LocalDate.of(2026, 8, 13),
				new Headcount(3),
				ScheduleCost.of(700_000L),
				TransportationType.RENTAL_CAR,
				TravelStyle.RELAXATION_HEALING,
				"가족 자유여행",
				"#22AA88"
		);

		assertThat(updated.scheduleUuid()).isEqualTo(schedule.scheduleUuid());
		assertThat(updated.memberUuid()).isEqualTo(schedule.memberUuid());
		assertThat(updated.creatorType()).isEqualTo(schedule.creatorType());
		assertThat(updated.title()).isEqualTo("제주 휴가");
		assertThat(updated.destination()).isEqualTo("제주");
		assertThat(updated.period().startDate()).isEqualTo(schedule.period().startDate());
		assertThat(updated.period().endDate()).isEqualTo(LocalDate.of(2026, 8, 13));
		assertThat(updated.headcount().value()).isEqualTo(3);
		assertThat(updated.expectedCost().amount()).isEqualTo(700_000L);
		assertThat(updated.transportation()).isEqualTo(TransportationType.RENTAL_CAR);
		assertThat(updated.travelStyle()).isEqualTo(TravelStyle.RELAXATION_HEALING);
		assertThat(updated.content()).isEqualTo("가족 자유여행");
		assertThat(updated.calendarColor()).isEqualTo("#22AA88");
	}

	@Test
	void rejectsPeriodThatExcludesExistingItem() {
		Schedule schedule = createSchedule(List.of(createItem(2)));

		assertThatThrownBy(() -> schedule.update(
				null,
				null,
				null,
				LocalDate.of(2026, 8, 11),
				null,
				null,
				null,
				null,
				null,
				null
		)).isInstanceOf(InvalidScheduleException.class)
				.hasMessage("Schedule item day must be within the schedule period.");
	}

	private Schedule createSchedule(List<ScheduleItem> items) {
		return Schedule.create(
				new MemberUuid(UUID.randomUUID()),
				"부산 여행",
				"부산",
				LocalDate.of(2026, 8, 11),
				LocalDate.of(2026, 8, 12),
				new Headcount(2),
				ScheduleCost.of(500_000L),
				TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.TOUR_LANDMARK,
				"해운대 방문",
				"#3366FF",
				ScheduleCreatorType.USER,
				items
		);
	}

	private ScheduleItem createItem(int day) {
		return ScheduleItem.create(
				new DayNumber(day),
				ScheduleItemType.ETC,
				"자유 일정",
				null,
				null,
				null,
				ScheduleCost.zero()
		);
	}
}
