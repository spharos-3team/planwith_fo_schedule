package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiScheduleReviseRequest(
		@NotBlank
		@Size(max = 2000)
		String additionalRequest
) {
}
