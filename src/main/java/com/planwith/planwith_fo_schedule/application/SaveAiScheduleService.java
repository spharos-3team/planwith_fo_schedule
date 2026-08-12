package com.planwith.planwith_fo_schedule.application;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.port.in.SaveAiScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;
import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.GeoPoint;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleItemLocation;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;

@Service
public class SaveAiScheduleService implements SaveAiScheduleUseCase {

	private final ScheduleRepositoryPort scheduleRepositoryPort;

	public SaveAiScheduleService(ScheduleRepositoryPort scheduleRepositoryPort) {
		this.scheduleRepositoryPort = scheduleRepositoryPort;
	}

	@Override
	@Transactional
	public SaveAiScheduleResult save(SaveAiScheduleCommand command) {
		SaveAiScheduleCommand validatedCommand = Objects.requireNonNull(command, "AI schedule save command is required.");
		List<ScheduleItem> items = Objects.requireNonNull(
				validatedCommand.items(),
				"AI schedule items are required."
		).stream().map(this::toScheduleItem).toList();
		SchedulePeriod period = new SchedulePeriod(validatedCommand.startDate(), validatedCommand.endDate());
		AiScheduleDraftValidator.validate(period, items);

		Schedule savedSchedule = scheduleRepositoryPort.save(Schedule.create(
				new MemberUuid(validatedCommand.memberUuid()),
				validatedCommand.title(),
				validatedCommand.destination(),
				validatedCommand.startDate(),
				validatedCommand.endDate(),
				new Headcount(validatedCommand.participantCount()),
				ScheduleCost.of(validatedCommand.estimatedBudget()),
				validatedCommand.transportation(),
				validatedCommand.travelStyle(),
				validatedCommand.content(),
				validatedCommand.calendarColor(),
				ScheduleCreatorType.AI,
				items
		));

		return new SaveAiScheduleResult(
				savedSchedule.scheduleUuid().value(),
				savedSchedule.memberUuid().value(),
				savedSchedule.title(),
				savedSchedule.items().size()
		);
	}

	private ScheduleItem toScheduleItem(SaveAiScheduleItemCommand item) {
		Objects.requireNonNull(item, "AI schedule item is required.");
		return ScheduleItem.create(
				new DayNumber(item.dayNumber()),
				item.scheduleType(),
				item.subtitle(),
				item.description(),
				toLocation(item),
				item.scheduleTime(),
				ScheduleCost.of(item.estimatedCost())
		);
	}

	private ScheduleItemLocation toLocation(SaveAiScheduleItemCommand item) {
		GeoPoint coordinates = item.latitude() == null && item.longitude() == null
				? null
				: new GeoPoint(item.latitude(), item.longitude());
		if (item.placeName() == null && item.placeAddress() == null && coordinates == null) {
			return null;
		}
		return new ScheduleItemLocation(item.placeName(), item.placeAddress(), coordinates);
	}
}
