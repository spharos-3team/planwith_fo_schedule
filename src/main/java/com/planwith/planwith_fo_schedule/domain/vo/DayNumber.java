package com.planwith.planwith_fo_schedule.domain.vo;

import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;

public record DayNumber(int value) {

	public DayNumber {
		if (value < 1) {
			throw new InvalidScheduleException("Day number must be at least 1.");
		}
	}
}
