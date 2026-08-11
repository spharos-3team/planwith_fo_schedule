package com.planwith.planwith_fo_schedule.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;

class ScheduleAggregateRuleTest {

	@Test
	void rejectsItemOutsideSchedulePeriod() {
		assertThatThrownBy(() -> createSchedule(List.of(createItem(2))))
				.isInstanceOf(InvalidScheduleException.class)
				.hasMessage("Schedule item day must be within the schedule period.");
	}

	private Schedule createSchedule(List<ScheduleItem> items) {
		return Schedule.create(
				new MemberUuid(UUID.randomUUID()),
				"서울 당일 여행",
				"서울",
				LocalDate.of(2026, 8, 11),
				LocalDate.of(2026, 8, 11),
				new Headcount(1),
				ScheduleCost.zero(),
				null,
				null,
				null,
				ScheduleCreatorType.AI,
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
