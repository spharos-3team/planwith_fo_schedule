package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateScheduleRequest(
		@NotNull UUID memberUuid,
		@Size(max = 200) String title,
		@NotBlank @Size(max = 200) String destination,
		@NotNull LocalDate startDate,
		@NotNull LocalDate endDate,
		@NotNull @Min(1) Integer headcount,
		@PositiveOrZero Long expectedCost,
		TransportationType transportation,
		TravelStyle travelStyle,
		String content,
		@Size(max = 30) String calendarColor
) {
}
