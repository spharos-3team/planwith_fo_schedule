package com.planwith.planwith_fo_schedule.adapter.out.openai;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;

@JsonIgnoreProperties(ignoreUnknown = true)
record OpenAiGeneratedSchedulePayload(String title, String content, List<Item> items) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Item(
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
