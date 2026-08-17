package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.command.AiScheduleGenerateCommand;
import com.planwith.planwith_fo_schedule.application.exception.AiScheduleGenerationException;
import com.planwith.planwith_fo_schedule.application.model.AiOperationType;
import com.planwith.planwith_fo_schedule.application.model.AiUsageReportEvent;
import com.planwith.planwith_fo_schedule.application.model.OpenAiUsage;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleGenerationPort;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleGenerationPort.GeneratedScheduleItem;
import com.planwith.planwith_fo_schedule.application.port.out.AiUsageReportingPort;
import com.planwith.planwith_fo_schedule.application.port.out.DestinationImageSearchPort;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;

class AiScheduleApplicationServiceTest {

	@Test
	void callsAiPortAgainWithSameConditionsForRegeneration() {
		AtomicInteger callCount = new AtomicInteger();
		AiScheduleGenerationPort port = command -> new AiScheduleGenerationPort.GeneratedAiSchedule(
				"AI 일정 초안 " + callCount.incrementAndGet(),
				null,
				List.of(
						item(1, LocalTime.of(10, 0), "첫째 날 일정"),
						item(2, LocalTime.of(10, 0), "둘째 날 일정"),
						item(3, LocalTime.of(10, 0), "셋째 날 일정")
				),
				new OpenAiUsage("gpt-4o-mini-2024-07-18", 100, 50, 150)
		);
		AiScheduleApplicationService service = service(port, noImageSearch());
		AiScheduleGenerateCommand sameConditions = command();

		var firstDraft = service.generate(sameConditions);
		var regeneratedDraft = service.generate(sameConditions, AiOperationType.REGENERATE);

		assertThat(callCount).hasValue(2);
		assertThat(firstDraft.title()).isEqualTo("AI 일정 초안 1");
		assertThat(regeneratedDraft.title()).isEqualTo("AI 일정 초안 2");
		assertThat(firstDraft.usage().operationType()).isEqualTo(AiOperationType.GENERATE);
		assertThat(regeneratedDraft.usage().operationType()).isEqualTo(AiOperationType.REGENERATE);
		assertThat(regeneratedDraft.usage().requestId()).isNotEqualTo(firstDraft.usage().requestId());
	}

	@Test
	void returnsDraftItemsForEveryTravelDayInChronologicalOrder() {
		AiScheduleGenerationPort port = command -> new AiScheduleGenerationPort.GeneratedAiSchedule(
				"부산 AI 여행",
				null,
				List.of(
						item(2, LocalTime.of(14, 0), "감천문화마을 관람"),
						item(1, LocalTime.of(10, 0), "해운대 산책"),
						item(3, LocalTime.of(11, 0), "부산역 이동"),
						item(2, LocalTime.of(9, 0), "자갈치시장 방문")
				),
				new OpenAiUsage("gpt-4o-mini-2024-07-18", 120, 80, 200)
		);
		AtomicReference<AiUsageReportEvent> reportedEvent = new AtomicReference<>();
		AiScheduleApplicationService service = service(port, imageSearchWithUsage(), reportedEvent::set);

		AiScheduleGenerateCommand command = command();
		var result = service.generate(command);

		assertThat(result.title()).isEqualTo("부산 AI 여행");
		assertThat(result.imageUrl()).isEqualTo("https://images.example.com/busan.jpg");
		assertThat(result.scheduleUsage().totalTokens()).isEqualTo(200);
		assertThat(result.imageUsage().model()).isEqualTo("gpt-5.6-2026-08-01");
		assertThat(result.imageUsage().totalTokens()).isEqualTo(60);
		assertThat(result.usage().memberUuid()).isEqualTo(command.memberUuid().value());
		assertThat(result.usage().requestId()).isNotNull();
		assertThat(result.usage().operationType()).isEqualTo(AiOperationType.GENERATE);
		assertThat(result.usage().model()).isEqualTo("gpt-4o-mini-2024-07-18,gpt-5.6-2026-08-01");
		assertThat(result.usage().inputTokens()).isEqualTo(165);
		assertThat(result.usage().outputTokens()).isEqualTo(95);
		assertThat(result.usage().totalTokens()).isEqualTo(260);
		assertThat(reportedEvent.get().memberUuid()).isEqualTo(result.usage().memberUuid());
		assertThat(reportedEvent.get().requestId()).isEqualTo(result.usage().requestId());
		assertThat(reportedEvent.get().operationType()).isEqualTo(AiOperationType.GENERATE);
		assertThat(reportedEvent.get().model()).isEqualTo(result.usage().model());
		assertThat(reportedEvent.get().inputTokens()).isEqualTo(165);
		assertThat(reportedEvent.get().outputTokens()).isEqualTo(95);
		assertThat(reportedEvent.get().totalTokens()).isEqualTo(260);
		assertThat(reportedEvent.get().occurredAt()).isNotNull();
		assertThat(result.items())
				.extracting(item -> "%d-%s".formatted(item.dayNumber(), item.scheduleTime()))
				.containsExactly("1-10:00", "2-09:00", "2-14:00", "3-11:00");
	}

	@Test
	void rejectsDraftWhenAnyTravelDayHasNoItem() {
		AiScheduleGenerationPort port = command -> new AiScheduleGenerationPort.GeneratedAiSchedule(
				"누락된 일정",
				null,
				List.of(
						item(1, LocalTime.of(10, 0), "첫날 일정"),
						item(3, LocalTime.of(10, 0), "마지막 날 일정")
				)
		);
		AiScheduleApplicationService service = service(port, noImageSearch());

		assertThatThrownBy(() -> service.generate(command()))
				.isInstanceOf(AiScheduleGenerationException.class)
				.hasMessageContaining("domain rules");
	}

	@Test
	void rejectsDraftItemOutsideRequestedPeriod() {
		AiScheduleGenerationPort port = command -> new AiScheduleGenerationPort.GeneratedAiSchedule(
				"범위를 벗어난 일정",
				null,
				List.of(item(4, LocalTime.NOON, "범위를 벗어난 일정"))
		);
		AiScheduleApplicationService service = service(port, noImageSearch());

		assertThatThrownBy(() -> service.generate(command()))
				.isInstanceOf(AiScheduleGenerationException.class)
				.hasMessageContaining("domain rules");
	}

	private GeneratedScheduleItem item(int dayNumber, LocalTime time, String subtitle) {
		return new GeneratedScheduleItem(
				dayNumber,
				time,
				subtitle,
				ScheduleItemType.TOUR,
				"여행자가 확인할 AI 일정 초안입니다.",
				0,
				null,
				null,
				null,
				null
		);
	}

	private DestinationImageSearchPort noImageSearch() {
		return destination -> Optional.empty();
	}

	private AiScheduleApplicationService service(
			AiScheduleGenerationPort generationPort,
			DestinationImageSearchPort imageSearchPort
	) {
		return service(generationPort, imageSearchPort, event -> {
		});
	}

	private AiScheduleApplicationService service(
			AiScheduleGenerationPort generationPort,
			DestinationImageSearchPort imageSearchPort,
			AiUsageReportingPort usageReportingPort
	) {
		return new AiScheduleApplicationService(
				generationPort,
				imageSearchPort,
				new AiUsageAggregator(),
				new AiRequestIdGenerator(),
				new AiUsageReportEventFactory(),
				usageReportingPort
		);
	}

	private DestinationImageSearchPort imageSearchWithUsage() {
		return new DestinationImageSearchPort() {
			@Override
			public Optional<String> searchRepresentativeImage(String destination) {
				return Optional.of("https://images.example.com/busan.jpg");
			}

			@Override
			public DestinationImageSearchResult searchRepresentativeImageWithUsage(String destination) {
				return new DestinationImageSearchResult(
						searchRepresentativeImage(destination),
						new OpenAiUsage("gpt-5.6-2026-08-01", 45, 15, 60)
				);
			}
		};
	}

	private AiScheduleGenerateCommand command() {
		return new AiScheduleGenerateCommand(
				new MemberUuid(UUID.randomUUID()),
				"부산",
				new SchedulePeriod(LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 22)),
				new Headcount(2),
				ScheduleCost.of(500_000),
				TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.TOUR_LANDMARK,
				"바다 중심 일정",
				null
		);
	}
}
