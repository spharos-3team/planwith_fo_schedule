package com.planwith.planwith_fo_schedule.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ScheduleItem(
		UUID id,
		String title,
		Instant startsAt,
		Instant endsAt
) {

	public ScheduleItem {
		Objects.requireNonNull(id, "Schedule item ID is required.");
		Objects.requireNonNull(startsAt, "Start time is required.");
		Objects.requireNonNull(endsAt, "End time is required.");
		if (title == null || title.isBlank()) {
			throw new InvalidScheduleException("Schedule item title is required.");
		}
		title = title.trim();
		if (!startsAt.isBefore(endsAt)) {
			throw new InvalidScheduleException("End time must be after start time.");
		}
	}

	public static ScheduleItem create(String title, Instant startsAt, Instant endsAt) {
		return new ScheduleItem(UUID.randomUUID(), title, startsAt, endsAt);
	}
}
