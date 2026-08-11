package com.planwith.planwith_fo_schedule.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

public interface ScheduleRepositoryPort {

	Schedule save(Schedule schedule);

	Schedule update(Schedule schedule);

	Schedule softDelete(Schedule schedule);

	Optional<Schedule> findByScheduleUuid(ScheduleUuid scheduleUuid);
}
