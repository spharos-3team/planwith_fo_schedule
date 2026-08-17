package com.planwith.planwith_fo_schedule.application;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_schedule.application.command.AiScheduleGenerateCommand;
import com.planwith.planwith_fo_schedule.application.exception.AiScheduleGenerationException;
import com.planwith.planwith_fo_schedule.application.model.AiOperationType;
import com.planwith.planwith_fo_schedule.application.model.AiUsageResult;
import com.planwith.planwith_fo_schedule.application.model.OpenAiUsage;
import com.planwith.planwith_fo_schedule.application.port.in.GenerateAiScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleGenerationPort;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleGenerationPort.GeneratedScheduleItem;
import com.planwith.planwith_fo_schedule.application.port.out.DestinationImageSearchPort;
import com.planwith.planwith_fo_schedule.application.port.out.DestinationImageSearchPort.DestinationImageSearchResult;
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

	private static final Logger log = LoggerFactory.getLogger(AiScheduleApplicationService.class);

	private final AiScheduleGenerationPort aiScheduleGenerationPort;
	private final DestinationImageSearchPort destinationImageSearchPort;
	private final AiUsageAggregator aiUsageAggregator;
	private final AiRequestIdGenerator requestIdGenerator;

	public AiScheduleApplicationService(
			AiScheduleGenerationPort aiScheduleGenerationPort,
			DestinationImageSearchPort destinationImageSearchPort,
			AiUsageAggregator aiUsageAggregator,
			AiRequestIdGenerator requestIdGenerator
	) {
		this.aiScheduleGenerationPort = aiScheduleGenerationPort;
		this.destinationImageSearchPort = destinationImageSearchPort;
		this.aiUsageAggregator = aiUsageAggregator;
		this.requestIdGenerator = requestIdGenerator;
	}

	@Override
	public AiScheduleResult generate(AiScheduleGenerateCommand command, AiOperationType operationType) {
		requireGenerationOperation(operationType);
		UUID requestId = requestIdGenerator.generate();
		log.info("AiScheduleApplicationService : generate : AI 일정 생성 비즈니스 로직 시작 - memberUuid={}, "
						+ "requestId={}, operationType={}",
				command.memberUuid().value(), requestId, operationType);
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
			DestinationImageSearchResult imageSearchResult =
					destinationImageSearchPort.searchRepresentativeImageWithUsage(command.destination());
			String imageUrl = imageSearchResult.imageUrl().orElse(null);
			AiUsageResult usage = aiUsageAggregator.aggregate(
					schedule.memberUuid().value(),
					requestId,
					operationType,
					availableUsages(generated.usage(), imageSearchResult.usage())
			);

			AiScheduleResult result = new AiScheduleResult(
					schedule.memberUuid().value(),
					schedule.title(),
					schedule.destination(),
					imageUrl,
					schedule.period().startDate(),
					schedule.period().endDate(),
					schedule.headcount().value(),
					schedule.expectedCost().amount(),
					schedule.transportation(),
					schedule.travelStyle(),
					schedule.content(),
					schedule.items().stream().map(this::toResultItem).toList(),
					generated.usage(),
					imageSearchResult.usage(),
					usage
			);
			log.info("AiScheduleApplicationService : generate : AI 일정 생성 비즈니스 로직 완료 - memberUuid={}, "
							+ "requestId={}, operationType={}, itemCount={}",
					result.memberUuid(), requestId, operationType, result.items().size());
			return result;
		} catch (InvalidScheduleException exception) {
			throw new AiScheduleGenerationException("AI returned a schedule that violates domain rules.", exception);
		}
	}

	private List<OpenAiUsage> availableUsages(OpenAiUsage... usages) {
		return Stream.of(usages)
				.filter(Objects::nonNull)
				.toList();
	}

	private void requireGenerationOperation(AiOperationType operationType) {
		if (operationType != AiOperationType.GENERATE && operationType != AiOperationType.REGENERATE) {
			throw new IllegalArgumentException("AI schedule generation operation must be GENERATE or REGENERATE.");
		}
	}

	private List<ScheduleItem> normalizeItems(List<GeneratedScheduleItem> generatedItems) {
		return generatedItems.stream()
				.map(this::toDomainItem)
				.sorted(Comparator
						.comparingInt((ScheduleItem item) -> item.day().value())
						.thenComparing(item -> item.startTime(), Comparator.nullsLast(Comparator.naturalOrder())))
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
