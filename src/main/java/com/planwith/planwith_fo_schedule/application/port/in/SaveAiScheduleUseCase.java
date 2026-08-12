package com.planwith.planwith_fo_schedule.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

public interface SaveAiScheduleUseCase {

	SaveAiScheduleResult save(SaveAiScheduleCommand command);

	record SaveAiScheduleCommand(
			UUID memberUuid,
			String title,
			String destination,
			LocalDate startDate,
			LocalDate endDate,
			Integer participantCount,
			Long estimatedBudget,
			TransportationType transportation,
			TravelStyle travelStyle,
			String content,
			String calendarColor,
			List<SaveAiScheduleItemCommand> items
	) {
		public SaveAiScheduleCommand {
			items = items == null ? null : List.copyOf(items);
		}
	}

	record SaveAiScheduleItemCommand(
			Integer dayNumber,
			LocalTime scheduleTime,
			String subtitle,
			ScheduleItemType scheduleType,
			String description,
			Long estimatedCost,
			String placeName,
			String placeAddress,
			BigDecimal latitude,
			BigDecimal longitude
	) {
	}

	record SaveAiScheduleResult(
			UUID scheduleUuid,
			UUID memberUuid,
			String title,
			int itemCount
	) {
	}
}
