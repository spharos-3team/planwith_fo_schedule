package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;

public record UpdateScheduleResponse(
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
		ScheduleCreatorType creatorType
) {
}
