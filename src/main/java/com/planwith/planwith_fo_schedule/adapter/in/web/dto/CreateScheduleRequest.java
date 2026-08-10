package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateScheduleRequest(
		@NotNull UUID ownerId,
		@NotBlank @Size(max = 100) String title,
		@NotNull List<@NotNull @Valid ScheduleItemRequest> items
) {

	public record ScheduleItemRequest(
			@NotBlank @Size(max = 100) String title,
			@NotNull Instant startsAt,
			@NotNull Instant endsAt
	) {
	}
}
