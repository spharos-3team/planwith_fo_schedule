package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;

interface CalendarScheduleProjection {

	UUID getScheduleUuid();

	String getTitle();

	LocalDate getStartDate();

	LocalDate getEndDate();

	String getCalendarColor();

	ScheduleCreatorType getCreatorType();
}
