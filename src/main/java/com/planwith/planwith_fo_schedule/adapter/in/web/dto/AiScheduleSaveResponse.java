package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.util.UUID;

public record AiScheduleSaveResponse(
		UUID scheduleUuid,
		UUID memberUuid,
		String title,
		int itemCount,
		boolean flightSaved,
		int flightSegmentCount
) {
}
