package com.planwith.planwith_fo_schedule.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;

public interface UpdateScheduleUseCase {

	UpdateScheduleResult updateSchedule(UUID scheduleUuid, UpdateScheduleCommand command);

	record UpdateScheduleCommand(
			String title,
			String destination,
			LocalDate startDate,
			LocalDate endDate,
			Integer headcount,
			Long expectedCost,
			String transportation,
			String content,
			String calendarColor
	) {
	}

	record UpdateScheduleResult(
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
}
