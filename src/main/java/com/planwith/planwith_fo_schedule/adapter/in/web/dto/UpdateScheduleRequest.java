package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateScheduleRequest(
		@Size(max = 200) String title,
		@Size(max = 200)
		@Pattern(regexp = "(?s).*\\S.*", message = "destination must not be blank")
		String destination,
		LocalDate startDate,
		LocalDate endDate,
		@Min(1) Integer headcount,
		@PositiveOrZero Long expectedCost,
		String transportation,
		String content,
		@Size(max = 30) String calendarColor
) {
}
