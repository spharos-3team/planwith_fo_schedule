package com.planwith.planwith_fo_schedule.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.CreatorType;

public interface GetScheduleDetailUseCase {

	ScheduleDetailResult getScheduleDetail(UUID scheduleUuid);

	record ScheduleDetailResult(
			UUID scheduleUuid,
			String title,
			String destination,
			LocalDate startDate,
			LocalDate endDate,
			int headcount,
			Long expectedCost,
			String transportation,
			String content,
			String calendarColor,
			CreatorType creatorType
	) {
	}
}
