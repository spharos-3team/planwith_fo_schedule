package com.planwith.planwith_fo_schedule.application;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.exception.ScheduleNotFoundException;
import com.planwith.planwith_fo_schedule.application.port.in.DeleteScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

@Service
public class DeleteScheduleService implements DeleteScheduleUseCase {

	private final ScheduleRepositoryPort scheduleRepositoryPort;

	public DeleteScheduleService(ScheduleRepositoryPort scheduleRepositoryPort) {
		this.scheduleRepositoryPort = scheduleRepositoryPort;
	}

	@Override
	@Transactional
	public void deleteSchedule(UUID scheduleUuid) {
		UUID validatedScheduleUuid = Objects.requireNonNull(scheduleUuid, "Schedule UUID is required.");
		Schedule schedule = scheduleRepositoryPort.findByScheduleUuid(new ScheduleUuid(validatedScheduleUuid))
				.orElseThrow(() -> new ScheduleNotFoundException(validatedScheduleUuid));

		scheduleRepositoryPort.softDelete(schedule.delete(LocalDateTime.now(ZoneOffset.UTC)));
	}
}
