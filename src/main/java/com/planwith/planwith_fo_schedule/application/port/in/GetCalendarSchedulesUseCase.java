package com.planwith.planwith_fo_schedule.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;

public interface GetCalendarSchedulesUseCase {

	List<CalendarScheduleResult> getCalendarSchedules(UUID memberUuid, LocalDate startDate, LocalDate endDate);

	record CalendarScheduleResult(
			UUID scheduleUuid,
			String title,
			LocalDate startDate,
			LocalDate endDate,
			String calendarColor,
			ScheduleCreatorType creatorType
	) {
	}
}
