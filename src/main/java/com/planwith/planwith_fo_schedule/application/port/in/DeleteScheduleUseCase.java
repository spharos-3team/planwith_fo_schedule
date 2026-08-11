package com.planwith.planwith_fo_schedule.application.port.in;

import java.util.UUID;

public interface DeleteScheduleUseCase {

	void deleteSchedule(UUID scheduleUuid);
}
