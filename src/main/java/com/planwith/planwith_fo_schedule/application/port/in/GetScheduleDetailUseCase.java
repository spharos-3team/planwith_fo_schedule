package com.planwith.planwith_fo_schedule.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

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
			TransportationType transportation,
			TravelStyle travelStyle,
			String content,
			String calendarColor,
			ScheduleCreatorType creatorType
	) {
	}
}
