package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.port.in.SaveAiScheduleUseCase.SaveAiScheduleCommand;
import com.planwith.planwith_fo_schedule.application.port.in.SaveAiScheduleUseCase.SaveAiScheduleItemCommand;
import com.planwith.planwith_fo_schedule.application.port.in.SaveAiScheduleUseCase.SaveAiScheduleFlightCommand;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate.AirportSchedule;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.TripType;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

class SaveAiScheduleServiceTest {

	@Test
	void savesConfirmedDraftWithAiCreatorTypeUsingScheduleAggregate() {
		CapturingScheduleRepository repository = new CapturingScheduleRepository();
		SaveAiScheduleService service = new SaveAiScheduleService(repository, new FlightDomainMapper());
		UUID memberUuid = UUID.randomUUID();

		var result = service.save(new SaveAiScheduleCommand(
				memberUuid,
				"부산 AI 여행",
				"부산",
				LocalDate.of(2026, 8, 20),
				LocalDate.of(2026, 8, 20),
				2,
				500_000L,
				TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.TOUR_LANDMARK,
				null,
				"#4F46E5",
				List.of(new SaveAiScheduleItemCommand(
						1,
						LocalTime.of(10, 0),
						"해운대 산책",
						ScheduleItemType.TOUR,
						"해변을 산책합니다.",
						0L,
						"해운대",
						"부산광역시 해운대구",
						new BigDecimal("35.1587000"),
						new BigDecimal("129.1604000")
				)),
				null
		));

		Schedule saved = repository.savedSchedule;
		assertThat(saved).isNotNull();
		assertThat(saved.memberUuid().value()).isEqualTo(memberUuid);
		assertThat(saved.creatorType()).isEqualTo(ScheduleCreatorType.AI);
		assertThat(saved.items()).hasSize(1);
		assertThat(saved.items().get(0).itemType()).isEqualTo(ScheduleItemType.TOUR);
		assertThat(result.scheduleUuid()).isEqualTo(saved.scheduleUuid().value());
		assertThat(result.itemCount()).isEqualTo(1);
		assertThat(result.flightSaved()).isFalse();
		assertThat(result.flightSegmentCount()).isZero();
	}

	@Test
	void savesSelectedFlightAndSegmentsWithScheduleAggregate() {
		CapturingScheduleRepository repository = new CapturingScheduleRepository();
		SaveAiScheduleService service = new SaveAiScheduleService(repository, new FlightDomainMapper());
		FlightCandidate outbound = candidate(
				"ICN", "NRT", "2026-08-20T09:00:00+09:00", "2026-08-20T11:30:00+09:00", "703"
		);
		FlightCandidate inbound = candidate(
				"NRT", "ICN", "2026-08-22T18:00:00+09:00", "2026-08-22T20:30:00+09:00", "704"
		);

		var result = service.save(new SaveAiScheduleCommand(
				UUID.randomUUID(), "Tokyo AI trip", "Tokyo",
				LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 22), 2, 500_000L,
				TransportationType.OTHER, TravelStyle.TOUR_LANDMARK, null, "#4F46E5",
				List.of(
						item(1, "Arrival"),
						item(2, "Tokyo tour"),
						item(3, "Departure")
				),
				new SaveAiScheduleFlightCommand(
						"Seoul", TripType.ROUND_TRIP, List.of(outbound), List.of(inbound)
				)
		));

		Schedule saved = repository.savedSchedule;
		assertThat(saved.flight()).isNotNull();
		assertThat(saved.flight().segments()).hasSize(2);
		assertThat(saved.flight().originLocationCode()).isEqualTo("ICN");
		assertThat(saved.flight().destinationLocationCode()).isEqualTo("NRT");
		assertThat(result.flightSaved()).isTrue();
		assertThat(result.flightSegmentCount()).isEqualTo(2);
	}

	private SaveAiScheduleItemCommand item(int dayNumber, String title) {
		return new SaveAiScheduleItemCommand(
				dayNumber, LocalTime.of(10, 0), title, ScheduleItemType.TOUR,
				"Description", 0L, null, null, null, null
		);
	}

	private FlightCandidate candidate(
			String departureCode,
			String arrivalCode,
			String departureAt,
			String arrivalAt,
			String flightNumber
	) {
		OffsetDateTime departureTime = OffsetDateTime.parse(departureAt);
		OffsetDateTime arrivalTime = OffsetDateTime.parse(arrivalAt);
		return new FlightCandidate(
				departureTime.toLocalDate(), "scheduled",
				new AirportSchedule(departureCode, "1", "10", departureTime, timezone(departureCode)),
				new AirportSchedule(arrivalCode, "2", "20", arrivalTime, timezone(arrivalCode)),
				"KE", flightNumber, "KE", "B789", 150L
		);
	}

	private String timezone(String airportCode) {
		return "ICN".equals(airportCode) ? "Asia/Seoul" : "Asia/Tokyo";
	}

	private static final class CapturingScheduleRepository implements ScheduleRepositoryPort {
		private Schedule savedSchedule;

		@Override
		public Schedule save(Schedule schedule) {
			this.savedSchedule = schedule;
			return schedule;
		}

		@Override
		public Schedule update(Schedule schedule) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Schedule softDelete(Schedule schedule) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<Schedule> findByScheduleUuid(ScheduleUuid scheduleUuid) {
			return Optional.empty();
		}
	}
}
