package com.planwith.planwith_fo_schedule.application.port.out;

import java.time.LocalDate;

import com.planwith.planwith_fo_schedule.application.model.OpenAiUsage;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

public interface AiScheduleRevisionPort {

	RevisedSchedule revise(ScheduleRevisionContext context);

	record ScheduleRevisionContext(
			String title,
			String destination,
			LocalDate startDate,
			LocalDate endDate,
			int headcount,
			long expectedCost,
			TransportationType transportation,
			TravelStyle travelStyle,
			String content,
			String additionalRequest
	) {
	}

	record RevisedSchedule(
			String content,
			OpenAiUsage usage
	) {
		public RevisedSchedule(String content) {
			this(content, null);
		}
	}
}
