package com.planwith.planwith_fo_schedule.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;

@Service
public class ScheduleApplicationService implements CreateScheduleUseCase {

	private final ScheduleRepositoryPort scheduleRepositoryPort;

	public ScheduleApplicationService(ScheduleRepositoryPort scheduleRepositoryPort) {
		this.scheduleRepositoryPort = scheduleRepositoryPort;
	}

	@Override
	@Transactional
	public CreateScheduleResult createSchedule(CreateScheduleCommand command) {
		List<ScheduleItem> items = command.items().stream()
				.map(item -> ScheduleItem.create(item.title(), item.startsAt(), item.endsAt()))
				.toList();
		Schedule savedSchedule = scheduleRepositoryPort.save(
				Schedule.create(command.ownerId(), command.title(), items)
		);

		return new CreateScheduleResult(
				savedSchedule.id(),
				savedSchedule.ownerId(),
				savedSchedule.title(),
				savedSchedule.items().size()
		);
	}
}
