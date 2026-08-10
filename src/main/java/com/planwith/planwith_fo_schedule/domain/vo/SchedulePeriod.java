package com.planwith.planwith_fo_schedule.domain.vo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;

public record SchedulePeriod(LocalDate startDate, LocalDate endDate) {

	public SchedulePeriod {
		if (startDate == null) {
			throw new InvalidScheduleException("Start date is required.");
		}
		if (endDate == null) {
			throw new InvalidScheduleException("End date is required.");
		}
		if (endDate.isBefore(startDate)) {
			throw new InvalidScheduleException("End date must not be before start date.");
		}
	}

	public long numberOfDays() {
		return ChronoUnit.DAYS.between(startDate, endDate) + 1;
	}
}
