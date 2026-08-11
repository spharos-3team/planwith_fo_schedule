package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

public record UpdateScheduleResponse(
		UUID scheduleUuid,
		String title,
		String destination,
		LocalDate startDate,
		LocalDate endDate,
		int headcount,
		Long expectedCost,
		TransportationType transportation,
		TravelStyle travelStyle,
		String content,
		String calendarColor,
		ScheduleCreatorType creatorType
) {
}
