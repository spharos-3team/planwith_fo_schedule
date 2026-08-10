package com.planwith.planwith_fo_schedule.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.CreatorType;
import com.planwith.planwith_fo_schedule.domain.ScheduleType;

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
			String transportation,
			String content,
			String calendarColor,
			CreatorType creatorType,
			List<CreateScheduleItemCommand> items
	) {
		public CreateScheduleCommand {
			items = List.copyOf(items);
		}
	}

	record CreateScheduleItemCommand(
			int dayNumber,
			LocalTime scheduleTime,
			String subtitle,
			ScheduleType scheduleType,
			String description,
			Long estimatedCost,
			String placeName,
			String placeAddress,
			BigDecimal latitude,
			BigDecimal longitude
	) {
	}

	record CreateScheduleResult(UUID scheduleUuid, UUID memberUuid, String title, int itemCount) {
	}
}
