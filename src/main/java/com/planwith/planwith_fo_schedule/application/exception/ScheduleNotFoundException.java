package com.planwith.planwith_fo_schedule.application.exception;

import java.util.UUID;

public class ScheduleNotFoundException extends RuntimeException {
	private final UUID scheduleUuid;

	public ScheduleNotFoundException(UUID scheduleUuid) {
		super("Schedule not found.");
		this.scheduleUuid = scheduleUuid;
	}

	public UUID scheduleUuid() {
		return scheduleUuid;
	}
}
