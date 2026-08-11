package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AiScheduleGenerateRequest(
		@NotBlank @Size(max = 200) String destination,
		@NotNull LocalDate startDate,
		@NotNull LocalDate endDate,
		@NotNull @Min(1) Integer participantCount,
		@NotNull @PositiveOrZero Long estimatedBudget,
		TransportationType transportation,
		TravelStyle travelStyle,
		@Size(max = 2_000) String additionalRequest,
		@Valid AiScheduleFlightRequest flight
) {

	@JsonIgnore
	@AssertTrue(message = "startDate must not be after endDate")
	public boolean isTravelPeriodValid() {
		return startDate == null || endDate == null || !startDate.isAfter(endDate);
	}
}
