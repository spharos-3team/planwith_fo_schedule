package com.planwith.planwith_fo_schedule.domain.vo;

import java.util.Objects;
import java.util.UUID;

public record ScheduleUuid(UUID value) {

	public ScheduleUuid {
		Objects.requireNonNull(value, "Schedule UUID is required.");
	}

	public static ScheduleUuid create() {
		return new ScheduleUuid(UUID.randomUUID());
	}

	public static ScheduleUuid from(String value) {
		return new ScheduleUuid(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
