package com.planwith.planwith_fo_schedule.domain.vo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;

public record SchedulePeriod(LocalDate startDate, LocalDate endDate) {

	public SchedulePeriod {
		Objects.requireNonNull(startDate, "Start date is required.");
		Objects.requireNonNull(endDate, "End date is required.");
		if (endDate.isBefore(startDate)) {
			throw new InvalidScheduleException("End date must not be before start date.");
		}
	}

	public long numberOfDays() {
		return ChronoUnit.DAYS.between(startDate, endDate) + 1;
	}
}
