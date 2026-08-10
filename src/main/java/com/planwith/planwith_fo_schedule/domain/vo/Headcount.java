package com.planwith.planwith_fo_schedule.domain.vo;

import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;

public record Headcount(int value) {

	public static final int DEFAULT_VALUE = 1;

	public Headcount {
		if (value < 1) {
			throw new InvalidScheduleException("Headcount must be at least 1.");
		}
	}

	public static Headcount defaultValue() {
		return new Headcount(DEFAULT_VALUE);
	}
}
