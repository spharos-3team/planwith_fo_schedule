package com.planwith.planwith_fo_schedule.domain.vo;

import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;

public record ScheduleCost(Long amount) {

	public ScheduleCost {
		if (amount != null && amount < 0) {
			throw new InvalidScheduleException("Schedule cost must not be negative.");
		}
	}

	public static ScheduleCost of(long amount) {
		return new ScheduleCost(amount);
	}

	public static ScheduleCost zero() {
		return of(0L);
	}

	public static ScheduleCost unspecified() {
		return new ScheduleCost(null);
	}

	public boolean isSpecified() {
		return amount != null;
	}
}
