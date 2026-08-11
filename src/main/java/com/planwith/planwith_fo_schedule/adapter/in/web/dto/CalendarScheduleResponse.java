package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.CreatorType;

public record CalendarScheduleResponse(
		UUID scheduleUuid,
		String title,
		LocalDate startDate,
		LocalDate endDate,
		String calendarColor,
		CreatorType creatorType
) {
}
