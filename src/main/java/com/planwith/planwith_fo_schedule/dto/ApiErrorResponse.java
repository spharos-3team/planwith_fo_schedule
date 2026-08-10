package com.planwith.planwith_fo_schedule.dto;

import java.time.Instant;

public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String code,
		String message
) {
}
