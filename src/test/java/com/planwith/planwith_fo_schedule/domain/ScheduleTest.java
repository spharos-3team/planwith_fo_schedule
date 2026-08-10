package com.planwith.planwith_fo_schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ScheduleTest {

	@Test
	void createsValidAggregate() {
		ScheduleItem item = ScheduleItem.create(
				"팀 회의",
				Instant.parse("2026-08-10T01:00:00Z"),
				Instant.parse("2026-08-10T02:00:00Z")
		);

		Schedule schedule = Schedule.create(UUID.randomUUID(), "프로젝트 일정", List.of(item));

		assertThat(schedule.id()).isNotNull();
		assertThat(schedule.items()).containsExactly(item);
	}

	@Test
	void rejectsInvalidItemTimeRange() {
		Instant startsAt = Instant.parse("2026-08-10T02:00:00Z");

		assertThatThrownBy(() -> ScheduleItem.create("팀 회의", startsAt, startsAt))
				.isInstanceOf(InvalidScheduleException.class)
				.hasMessage("End time must be after start time.");
	}

	@Test
	void protectsItemsFromExternalMutation() {
		List<ScheduleItem> items = new ArrayList<>();
		Schedule schedule = Schedule.create(UUID.randomUUID(), "프로젝트 일정", items);

		items.add(ScheduleItem.create(
				"뒤늦게 추가",
				Instant.parse("2026-08-10T01:00:00Z"),
				Instant.parse("2026-08-10T02:00:00Z")
		));

		assertThat(schedule.items()).isEmpty();
	}
}
