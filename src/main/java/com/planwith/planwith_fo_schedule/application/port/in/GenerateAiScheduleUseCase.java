package com.planwith.planwith_fo_schedule.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.application.command.AiScheduleGenerateCommand;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

public interface GenerateAiScheduleUseCase {

	AiScheduleResult generate(AiScheduleGenerateCommand command);

	record AiScheduleResult(
			UUID memberUuid,
			String title,
			String destination,
			String imageUrl,
			LocalDate startDate,
			LocalDate endDate,
			int participantCount,
			long estimatedBudget,
			TransportationType transportation,
			TravelStyle travelStyle,
			String content,
			List<AiScheduleItemResult> items
	) {
		public AiScheduleResult {
			items = List.copyOf(items);
		}
	}

	record AiScheduleItemResult(
			int dayNumber,
			LocalTime scheduleTime,
			String subtitle,
			ScheduleItemType scheduleType,
			String description,
			long estimatedCost,
			String placeName,
			String placeAddress,
			BigDecimal latitude,
			BigDecimal longitude
	) {
	}
}
