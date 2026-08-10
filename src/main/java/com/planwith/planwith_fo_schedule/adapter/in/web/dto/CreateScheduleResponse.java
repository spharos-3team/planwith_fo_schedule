package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.util.UUID;

public record CreateScheduleResponse(
		UUID scheduleId,
		UUID ownerId,
		String title,
		int itemCount
) {
}
