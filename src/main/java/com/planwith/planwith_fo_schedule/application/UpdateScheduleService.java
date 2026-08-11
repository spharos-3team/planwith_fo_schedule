package com.planwith.planwith_fo_schedule.application;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.exception.ScheduleNotFoundException;
import com.planwith.planwith_fo_schedule.application.port.in.UpdateScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

@Service
public class UpdateScheduleService implements UpdateScheduleUseCase {

	private final ScheduleRepositoryPort scheduleRepositoryPort;

	public UpdateScheduleService(ScheduleRepositoryPort scheduleRepositoryPort) {
		this.scheduleRepositoryPort = scheduleRepositoryPort;
	}

	@Override
	@Transactional
	public UpdateScheduleResult updateSchedule(UUID scheduleUuid, UpdateScheduleCommand command) {
		UUID validatedScheduleUuid = Objects.requireNonNull(scheduleUuid, "Schedule UUID is required.");
		UpdateScheduleCommand validatedCommand = Objects.requireNonNull(command, "Update command is required.");
		Schedule existingSchedule = scheduleRepositoryPort
				.findByScheduleUuid(new ScheduleUuid(validatedScheduleUuid))
				.orElseThrow(() -> new ScheduleNotFoundException(validatedScheduleUuid));

		Schedule updatedSchedule = existingSchedule.update(
				validatedCommand.title(),
				validatedCommand.destination(),
				validatedCommand.startDate(),
				validatedCommand.endDate(),
				validatedCommand.headcount() == null ? null : new Headcount(validatedCommand.headcount()),
				validatedCommand.expectedCost() == null ? null : ScheduleCost.of(validatedCommand.expectedCost()),
				validatedCommand.transportation(),
				validatedCommand.travelStyle(),
				validatedCommand.content(),
				validatedCommand.calendarColor()
		);
		Schedule savedSchedule = scheduleRepositoryPort.update(updatedSchedule);

		return toResult(savedSchedule);
	}

	private UpdateScheduleResult toResult(Schedule schedule) {
		return new UpdateScheduleResult(
				schedule.scheduleUuid().value(),
				schedule.title(),
				schedule.destination(),
				schedule.period().startDate(),
				schedule.period().endDate(),
				schedule.headcount().value(),
				schedule.expectedCost().amount(),
				schedule.transportation(),
				schedule.travelStyle(),
				schedule.content(),
				schedule.calendarColor(),
				schedule.creatorType()
		);
	}
}
