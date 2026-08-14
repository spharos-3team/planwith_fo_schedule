package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.command.AiScheduleGenerateCommand;
import com.planwith.planwith_fo_schedule.application.exception.AiScheduleGenerationException;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleGenerationPort;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleGenerationPort.GeneratedScheduleItem;
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
				)
		);
		AiScheduleApplicationService service = new AiScheduleApplicationService(port, noImageSearch());
		AiScheduleGenerateCommand sameConditions = command();

		var firstDraft = service.generate(sameConditions);
		var regeneratedDraft = service.generate(sameConditions);

		assertThat(callCount).hasValue(2);
		assertThat(firstDraft.title()).isEqualTo("AI 일정 초안 1");
		assertThat(regeneratedDraft.title()).isEqualTo("AI 일정 초안 2");
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
				)
		);
		AiScheduleApplicationService service = new AiScheduleApplicationService(
				port,
				destination -> Optional.of("https://images.example.com/busan.jpg")
		);

		var result = service.generate(command());

		assertThat(result.title()).isEqualTo("부산 AI 여행");
		assertThat(result.imageUrl()).isEqualTo("https://images.example.com/busan.jpg");
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
		AiScheduleApplicationService service = new AiScheduleApplicationService(port, noImageSearch());

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
		AiScheduleApplicationService service = new AiScheduleApplicationService(port, noImageSearch());

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
