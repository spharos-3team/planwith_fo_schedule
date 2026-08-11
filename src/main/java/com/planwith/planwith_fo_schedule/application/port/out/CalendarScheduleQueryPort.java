package com.planwith.planwith_fo_schedule.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.CreatorType;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;

public interface CalendarScheduleQueryPort {

	List<CalendarScheduleData> findOverlappingSchedules(SchedulePeriod period);

	record CalendarScheduleData(
			UUID scheduleUuid,
			String title,
			LocalDate startDate,
			LocalDate endDate,
			String calendarColor,
			CreatorType creatorType
	) {
	}
}
