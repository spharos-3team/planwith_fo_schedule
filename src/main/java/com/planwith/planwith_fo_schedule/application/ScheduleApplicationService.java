package com.planwith.planwith_fo_schedule.application;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;
import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.GeoPoint;
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
		List<ScheduleItem> items = command.items().stream()
				.map(this::createScheduleItem)
				.toList();
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
				command.creatorType(),
				items
		));

		return new CreateScheduleResult(
				savedSchedule.scheduleUuid().value(),
				savedSchedule.memberUuid().value(),
				savedSchedule.title(),
				savedSchedule.items().size()
		);
	}

	private ScheduleItem createScheduleItem(CreateScheduleItemCommand command) {
		return ScheduleItem.create(
				new DayNumber(command.dayNumber()),
				command.scheduleTime(),
				command.subtitle(),
				command.scheduleType(),
				command.description(),
				command.estimatedCost() == null ? ScheduleCost.zero() : ScheduleCost.of(command.estimatedCost()),
				command.placeName(),
				command.placeAddress(),
				createLocation(command.latitude(), command.longitude())
		);
	}

	private GeoPoint createLocation(BigDecimal latitude, BigDecimal longitude) {
		if (latitude == null && longitude == null) {
			return null;
		}
		return new GeoPoint(latitude, longitude);
	}
}
