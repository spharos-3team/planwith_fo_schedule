package com.planwith.planwith_fo_schedule.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.CreatorType;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;

@Service
public class ScheduleApplicationService implements CreateScheduleUseCase {

	private final ScheduleRepositoryPort scheduleRepositoryPort;

	public ScheduleApplicationService(ScheduleRepositoryPort scheduleRepositoryPort) {
		this.scheduleRepositoryPort = scheduleRepositoryPort;
	}

	@Override
	@Transactional
	public CreateScheduleResult createSchedule(CreateScheduleCommand command) {
		Schedule savedSchedule = scheduleRepositoryPort.save(Schedule.create(
				new MemberUuid(command.memberUuid()),
				command.title(),
				command.destination(),
				command.startDate(),
				command.endDate(),
				command.headcount() == null ? null : new Headcount(command.headcount()),
				command.expectedCost() == null
						? ScheduleCost.unspecified()
						: ScheduleCost.of(command.expectedCost()),
				command.transportation(),
				command.content(),
				command.calendarColor(),
				CreatorType.SELF,
				List.of()
		));

		return new CreateScheduleResult(
				savedSchedule.scheduleUuid().value(),
				savedSchedule.memberUuid().value(),
				savedSchedule.title()
		);
	}
}
