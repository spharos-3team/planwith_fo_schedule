package com.planwith.planwith_fo_schedule.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.port.in.GetCalendarSchedulesUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.CalendarScheduleQueryPort;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;

@Service
public class GetCalendarSchedulesService implements GetCalendarSchedulesUseCase {

	private final CalendarScheduleQueryPort calendarScheduleQueryPort;

	public GetCalendarSchedulesService(CalendarScheduleQueryPort calendarScheduleQueryPort) {
		this.calendarScheduleQueryPort = calendarScheduleQueryPort;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CalendarScheduleResult> getCalendarSchedules(LocalDate startDate, LocalDate endDate) {
		SchedulePeriod period = new SchedulePeriod(startDate, endDate);
		return calendarScheduleQueryPort.findOverlappingSchedules(period).stream()
				.map(schedule -> new CalendarScheduleResult(
						schedule.scheduleUuid(),
						schedule.title(),
						schedule.startDate(),
						schedule.endDate(),
						schedule.calendarColor(),
						schedule.creatorType()
				))
				.toList();
	}
}
