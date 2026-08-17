package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

public record AiScheduleGenerateResponse(
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
		List<AiScheduleItemResponse> items,
		OpenAiUsageResponse scheduleUsage,
		OpenAiUsageResponse imageUsage
) {

	public record AiScheduleItemResponse(
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
