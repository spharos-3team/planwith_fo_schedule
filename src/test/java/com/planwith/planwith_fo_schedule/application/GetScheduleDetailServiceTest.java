package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.exception.ScheduleNotFoundException;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.FlightDirection;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.ScheduleFlight;
import com.planwith.planwith_fo_schedule.domain.ScheduleFlightSegment;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.TripType;
import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

class GetScheduleDetailServiceTest {

	@Test
	void returnsScheduleItemsAndFlightSegmentsAsOneDetail() {
		ScheduleRepositoryPort repository = mock(ScheduleRepositoryPort.class);
		GetScheduleDetailService service = new GetScheduleDetailService(repository);
		Schedule schedule = createSchedule().withFlight(createFlight());
		when(repository.findByScheduleUuid(schedule.scheduleUuid())).thenReturn(Optional.of(schedule));

		var result = service.getScheduleDetail(schedule.scheduleUuid().value());

		assertThat(result.schedule().scheduleUuid()).isEqualTo(schedule.scheduleUuid().value());
		assertThat(result.schedule().title()).isEqualTo("Busan trip");
		assertThat(result.schedule().destination()).isEqualTo("Busan");
		assertThat(result.schedule().startDate()).isEqualTo(LocalDate.of(2026, 9, 1));
		assertThat(result.schedule().endDate()).isEqualTo(LocalDate.of(2026, 9, 3));
		assertThat(result.schedule().headcount()).isEqualTo(2);
		assertThat(result.schedule().expectedCost()).isEqualTo(500_000L);
		assertThat(result.schedule().transportation()).isEqualTo(TransportationType.TRAIN_PUBLIC_TRANSIT);
		assertThat(result.schedule().travelStyle()).isEqualTo(TravelStyle.TOUR_LANDMARK);
		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).dayNumber()).isEqualTo(1);
		assertThat(result.items().get(0).subtitle()).isEqualTo("Haeundae");
		assertThat(result.flight()).isNotNull();
		assertThat(result.flight().outbound()).hasSize(1);
		assertThat(result.flight().returnSegments()).hasSize(1);
		assertThat(result.flight().outbound().get(0).flightNumber()).isEqualTo("1401");
		assertThat(result.flight().returnSegments().get(0).flightNumber()).isEqualTo("1402");
		verify(repository).findByScheduleUuid(schedule.scheduleUuid());
	}

	@Test
	void returnsNullFlightWhenScheduleHasNoSelectedFlight() {
		ScheduleRepositoryPort repository = mock(ScheduleRepositoryPort.class);
		GetScheduleDetailService service = new GetScheduleDetailService(repository);
		Schedule schedule = createSchedule();
		when(repository.findByScheduleUuid(schedule.scheduleUuid())).thenReturn(Optional.of(schedule));

		var result = service.getScheduleDetail(schedule.scheduleUuid().value());

		assertThat(result.flight()).isNull();
		assertThat(result.items()).hasSize(1);
	}

	@Test
	void throwsNotFoundWhenScheduleDoesNotExist() {
		ScheduleRepositoryPort repository = mock(ScheduleRepositoryPort.class);
		GetScheduleDetailService service = new GetScheduleDetailService(repository);
		UUID scheduleUuid = UUID.randomUUID();
		when(repository.findByScheduleUuid(new ScheduleUuid(scheduleUuid))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getScheduleDetail(scheduleUuid))
				.isInstanceOfSatisfying(
						ScheduleNotFoundException.class,
						exception -> assertThat(exception.scheduleUuid()).isEqualTo(scheduleUuid)
				);
	}

	private Schedule createSchedule() {
		ScheduleItem item = ScheduleItem.create(
				new DayNumber(1), ScheduleItemType.TOUR, "Haeundae", "Beach tour", null,
				LocalTime.of(10, 0), ScheduleCost.zero()
		);
		return Schedule.create(
				new MemberUuid(UUID.randomUUID()), "Busan trip", "Busan",
				LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3), new Headcount(2),
				ScheduleCost.of(500_000L), TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.TOUR_LANDMARK, "Visit Haeundae", "#3366FF",
				ScheduleCreatorType.USER, List.of(item)
		);
	}

	private ScheduleFlight createFlight() {
		ScheduleFlightSegment outbound = segment(
				FlightDirection.OUTBOUND, "GMP", "PUS", "2026-09-01T08:00:00+09:00",
				"2026-09-01T09:10:00+09:00", "1401"
		);
		ScheduleFlightSegment inbound = segment(
				FlightDirection.RETURN, "PUS", "GMP", "2026-09-03T19:00:00+09:00",
				"2026-09-03T20:10:00+09:00", "1402"
		);
		return ScheduleFlight.create(
				"AVIATIONSTACK", "Seoul", "GMP", "Busan", "PUS", TripType.ROUND_TRIP,
				List.of(outbound, inbound)
		);
	}

	private ScheduleFlightSegment segment(
			FlightDirection direction,
			String departure,
			String arrival,
			String departureAt,
			String arrivalAt,
			String flightNumber
	) {
		return ScheduleFlightSegment.create(
				direction, 1, departure, arrival, "1", "2", "10", "20",
				OffsetDateTime.parse(departureAt), OffsetDateTime.parse(arrivalAt),
				"Asia/Seoul", "Asia/Seoul", "KE", flightNumber, "KE", "B738", "scheduled", 70
		);
	}
}
