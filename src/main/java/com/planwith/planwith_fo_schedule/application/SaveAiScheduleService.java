package com.planwith.planwith_fo_schedule.application;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger log = LoggerFactory.getLogger(SaveAiScheduleService.class);

	private final ScheduleRepositoryPort scheduleRepositoryPort;
	private final FlightDomainMapper flightDomainMapper;

	public SaveAiScheduleService(
			ScheduleRepositoryPort scheduleRepositoryPort,
			FlightDomainMapper flightDomainMapper
	) {
		this.scheduleRepositoryPort = scheduleRepositoryPort;
		this.flightDomainMapper = flightDomainMapper;
	}

	@Override
	@Transactional
	public SaveAiScheduleResult save(SaveAiScheduleCommand command) {
		SaveAiScheduleCommand validatedCommand = Objects.requireNonNull(command, "AI schedule save command is required.");
		log.info("SaveAiScheduleService : save : AI 일정 및 선택 항공편 저장 시작 - flightSelected={}",
				validatedCommand.flight() != null);
		List<ScheduleItem> items = Objects.requireNonNull(
				validatedCommand.items(),
				"AI schedule items are required."
		).stream().map(this::toScheduleItem).toList();
		SchedulePeriod period = new SchedulePeriod(validatedCommand.startDate(), validatedCommand.endDate());
		AiScheduleDraftValidator.validate(period, items);

		Schedule schedule = Schedule.create(
				new MemberUuid(validatedCommand.memberUuid()),
				validatedCommand.title(),
				validatedCommand.destination(),
				validatedCommand.imageUrl(),
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
		);
		if (validatedCommand.flight() != null) {
			schedule = schedule.withFlight(flightDomainMapper.toScheduleFlight(
					validatedCommand.flight().departureLocation(),
					schedule.destination(),
					validatedCommand.flight().tripType(),
					validatedCommand.flight().outboundCandidates(),
					validatedCommand.flight().returnCandidates()
			));
		}

		Schedule savedSchedule = scheduleRepositoryPort.save(schedule);
		int flightSegmentCount = savedSchedule.flight() == null ? 0 : savedSchedule.flight().segments().size();
		log.info("SaveAiScheduleService : save : AI 일정 및 선택 항공편 저장 완료 - scheduleUuid={}, "
				+ "flightSaved={}, flightSegmentCount={}",
				savedSchedule.scheduleUuid().value(), savedSchedule.flight() != null, flightSegmentCount);

		return new SaveAiScheduleResult(
				savedSchedule.scheduleUuid().value(),
				savedSchedule.memberUuid().value(),
				savedSchedule.title(),
				savedSchedule.items().size(),
				savedSchedule.flight() != null,
				flightSegmentCount
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
