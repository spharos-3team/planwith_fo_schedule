package com.planwith.planwith_fo_schedule.application.port.out;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import com.planwith.planwith_fo_schedule.application.command.AiScheduleGenerateCommand;
import com.planwith.planwith_fo_schedule.application.model.OpenAiUsage;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;

public interface AiScheduleGenerationPort {

	GeneratedAiSchedule generate(AiScheduleGenerateCommand command);

	record GeneratedAiSchedule(
			String title,
			String content,
			List<GeneratedScheduleItem> items,
			OpenAiUsage usage
	) {
		public GeneratedAiSchedule {
			items = items == null ? List.of() : List.copyOf(items);
		}

		public GeneratedAiSchedule(String title, String content, List<GeneratedScheduleItem> items) {
			this(title, content, items, null);
		}
	}

	record GeneratedScheduleItem(
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
