package com.planwith.planwith_fo_schedule.application.port.out;

import com.planwith.planwith_fo_schedule.domain.Schedule;

public interface ScheduleRepositoryPort {

	Schedule save(Schedule schedule);
}
