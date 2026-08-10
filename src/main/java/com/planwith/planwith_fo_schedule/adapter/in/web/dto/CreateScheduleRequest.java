package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.CreatorType;
import com.planwith.planwith_fo_schedule.domain.ScheduleType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
		@Min(1) Integer headcount,
		@PositiveOrZero Long expectedCost,
		String transportation,
		String content,
		@Size(max = 30) String calendarColor,
		@NotNull CreatorType creatorType,
		@NotNull List<@NotNull @Valid ScheduleItemRequest> items
) {

	public record ScheduleItemRequest(
			@Min(1) int dayNumber,
			LocalTime scheduleTime,
			@NotBlank @Size(max = 200) String subtitle,
			@NotNull ScheduleType scheduleType,
			String description,
			@PositiveOrZero Long estimatedCost,
			@Size(max = 200) String placeName,
			@Size(max = 500) String placeAddress,
			@DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
			@DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude
	) {
	}
}
