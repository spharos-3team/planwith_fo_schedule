package com.planwith.planwith_fo_schedule.application;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.exception.ScheduleNotFoundException;
import com.planwith.planwith_fo_schedule.application.port.in.GetScheduleDetailUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

@Service
public class GetScheduleDetailService implements GetScheduleDetailUseCase {

	private final ScheduleRepositoryPort scheduleRepositoryPort;

	public GetScheduleDetailService(ScheduleRepositoryPort scheduleRepositoryPort) {
		this.scheduleRepositoryPort = scheduleRepositoryPort;
	}

	@Override
	@Transactional(readOnly = true)
	public ScheduleDetailResult getScheduleDetail(UUID scheduleUuid) {
		UUID validatedScheduleUuid = Objects.requireNonNull(scheduleUuid, "Schedule UUID is required.");
		Schedule schedule = scheduleRepositoryPort.findByScheduleUuid(new ScheduleUuid(validatedScheduleUuid))
				.orElseThrow(() -> new ScheduleNotFoundException(validatedScheduleUuid));

		return new ScheduleDetailResult(
				schedule.scheduleUuid().value(),
				schedule.title(),
				schedule.destination(),
				schedule.period().startDate(),
				schedule.period().endDate(),
				schedule.headcount().value(),
				schedule.expectedCost().amount(),
				schedule.transportation(),
				schedule.content(),
				schedule.calendarColor(),
				schedule.creatorType()
		);
	}
}
