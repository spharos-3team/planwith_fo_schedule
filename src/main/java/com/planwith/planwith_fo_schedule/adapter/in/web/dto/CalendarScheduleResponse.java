package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;

public record CalendarScheduleResponse(
		UUID scheduleUuid,
		String title,
		LocalDate startDate,
		LocalDate endDate,
		String calendarColor,
		ScheduleCreatorType creatorType
) {
}
