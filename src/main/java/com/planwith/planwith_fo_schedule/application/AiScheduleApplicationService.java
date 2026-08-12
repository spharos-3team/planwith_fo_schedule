package com.planwith.planwith_fo_schedule.application;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_schedule.application.command.AiScheduleGenerateCommand;
import com.planwith.planwith_fo_schedule.application.exception.AiScheduleGenerationException;
import com.planwith.planwith_fo_schedule.application.port.in.GenerateAiScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleGenerationPort;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleGenerationPort.GeneratedScheduleItem;
import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;
import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.GeoPoint;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleItemLocation;

@Service
public class AiScheduleApplicationService implements GenerateAiScheduleUseCase {

	private final AiScheduleGenerationPort aiScheduleGenerationPort;

	public AiScheduleApplicationService(AiScheduleGenerationPort aiScheduleGenerationPort) {
		this.aiScheduleGenerationPort = aiScheduleGenerationPort;
	}

	@Override
	public AiScheduleResult generate(AiScheduleGenerateCommand command) {
		AiScheduleGenerationPort.GeneratedAiSchedule generated = aiScheduleGenerationPort.generate(command);
		try {
			List<ScheduleItem> generatedItems = normalizeItems(generated.items());
			AiScheduleDraftValidator.validate(command.period(), generatedItems);
			Schedule schedule = Schedule.create(
					command.memberUuid(),
					generated.title(),
					command.destination(),
					command.period().startDate(),
					command.period().endDate(),
					command.participantCount(),
					command.estimatedBudget(),
					command.transportation(),
					command.travelStyle(),
					generated.content(),
					null,
					ScheduleCreatorType.AI,
					generatedItems
			);

			return new AiScheduleResult(
					schedule.memberUuid().value(),
					schedule.title(),
					schedule.destination(),
					schedule.period().startDate(),
					schedule.period().endDate(),
					schedule.headcount().value(),
					schedule.expectedCost().amount(),
					schedule.transportation(),
					schedule.travelStyle(),
					schedule.content(),
					schedule.items().stream().map(this::toResultItem).toList()
			);
		} catch (InvalidScheduleException exception) {
			throw new AiScheduleGenerationException("AI returned a schedule that violates domain rules.", exception);
		}
	}

	private List<ScheduleItem> normalizeItems(List<GeneratedScheduleItem> generatedItems) {
		return generatedItems.stream()
				.map(this::toDomainItem)
				.sorted(Comparator
						.comparingInt((ScheduleItem item) -> item.day().value())
						.thenComparing(ScheduleItem::startTime, Comparator.nullsLast(Comparator.naturalOrder())))
				.toList();
	}

	private ScheduleItem toDomainItem(GeneratedScheduleItem generated) {
		ScheduleItemLocation location = createLocation(generated);
		return ScheduleItem.create(
				new DayNumber(generated.dayNumber()),
				generated.scheduleType(),
				generated.subtitle(),
				generated.description(),
				location,
				generated.scheduleTime(),
				ScheduleCost.of(generated.estimatedCost())
		);
	}

	private AiScheduleItemResult toResultItem(ScheduleItem item) {
		ScheduleItemLocation location = item.location();
		return new AiScheduleItemResult(
				item.day().value(),
				item.startTime(),
				item.title(),
				item.itemType(),
				item.content(),
				item.expectedCost().amount(),
				location == null ? null : location.placeName(),
				location == null ? null : location.placeAddress(),
				location == null || location.coordinates() == null ? null : location.coordinates().latitude(),
				location == null || location.coordinates() == null ? null : location.coordinates().longitude()
		);
	}

	private ScheduleItemLocation createLocation(GeneratedScheduleItem item) {
		GeoPoint coordinates = item.latitude() == null && item.longitude() == null
				? null
				: new GeoPoint(item.latitude(), item.longitude());
		if (item.placeName() == null && item.placeAddress() == null && coordinates == null) {
			return null;
		}
		return new ScheduleItemLocation(item.placeName(), item.placeAddress(), coordinates);
	}
}
