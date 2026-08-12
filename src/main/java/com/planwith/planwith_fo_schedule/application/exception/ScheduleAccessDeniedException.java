package com.planwith.planwith_fo_schedule.application.exception;

import java.util.UUID;

public class ScheduleAccessDeniedException extends RuntimeException {

	private final UUID scheduleUuid;

	public ScheduleAccessDeniedException(UUID scheduleUuid) {
		super("The authenticated member cannot access this schedule.");
		this.scheduleUuid = scheduleUuid;
	}

	public UUID scheduleUuid() {
		return scheduleUuid;
	}
}
