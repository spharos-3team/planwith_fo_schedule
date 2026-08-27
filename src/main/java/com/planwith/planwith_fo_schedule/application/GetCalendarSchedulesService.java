package com.planwith.planwith_fo_schedule.application;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.exception.AuthenticationRequiredException;
import com.planwith.planwith_fo_schedule.application.port.in.GetCalendarSchedulesUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.CalendarScheduleQueryPort;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;

@Service
public class GetCalendarSchedulesService implements GetCalendarSchedulesUseCase {

	private final CalendarScheduleQueryPort calendarScheduleQueryPort;

	public GetCalendarSchedulesService(CalendarScheduleQueryPort calendarScheduleQueryPort) {
		this.calendarScheduleQueryPort = calendarScheduleQueryPort;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CalendarScheduleResult> getCalendarSchedules(UUID memberUuid, LocalDate startDate, LocalDate endDate) {
		if (memberUuid == null) {
			throw new AuthenticationRequiredException();
		}
		SchedulePeriod period = new SchedulePeriod(startDate, endDate);
		return calendarScheduleQueryPort.findOverlappingSchedules(new MemberUuid(memberUuid), period).stream()
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
