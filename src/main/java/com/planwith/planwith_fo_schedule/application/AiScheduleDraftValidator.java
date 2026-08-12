package com.planwith.planwith_fo_schedule.application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;

final class AiScheduleDraftValidator {

	private AiScheduleDraftValidator() {
	}

	static void validate(SchedulePeriod period, List<ScheduleItem> items) {
		if (items.isEmpty()) {
			throw new InvalidScheduleException("AI schedule must contain at least one schedule item.");
		}
		Set<Long> generatedDays = items.stream()
				.map(item -> (long) item.day().value())
				.collect(Collectors.toSet());
		boolean coversEveryDay = LongStream.rangeClosed(1, period.numberOfDays())
				.allMatch(generatedDays::contains);
		if (!coversEveryDay) {
			throw new InvalidScheduleException("AI schedule must contain at least one item for every travel day.");
		}
	}
}
