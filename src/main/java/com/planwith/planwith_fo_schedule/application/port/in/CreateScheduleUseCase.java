package com.planwith.planwith_fo_schedule.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

public interface CreateScheduleUseCase {

	CreateScheduleResult createSchedule(CreateScheduleCommand command);

	record CreateScheduleCommand(
			UUID memberUuid,
			String title,
			String destination,
			LocalDate startDate,
			LocalDate endDate,
			Integer headcount,
			Long expectedCost,
			TransportationType transportation,
			TravelStyle travelStyle,
			String content,
			String calendarColor
	) {
	}

	record CreateScheduleResult(UUID scheduleUuid, UUID memberUuid, String title) {
	}
}
