package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.util.UUID;

public record AiScheduleReviseResponse(
		UUID scheduleUuid,
		String revisedContent
) {
}
