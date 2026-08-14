package com.planwith.planwith_fo_schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;

class ScheduleFlightTest {

	@Test
	void representsRoundTripWithDirectOutboundAndStopoverReturnSegments() {
		ScheduleFlightSegment outbound = segment(
				FlightDirection.OUTBOUND, 1, "ICN", "NRT", "2026-09-01T09:00:00+09:00",
				"2026-09-01T11:30:00+09:00", "703"
		);
		ScheduleFlightSegment returnFirst = segment(
				FlightDirection.RETURN, 1, "NRT", "KIX", "2026-09-05T14:00:00+09:00",
				"2026-09-05T15:30:00+09:00", "100"
		);
		ScheduleFlightSegment returnSecond = segment(
				FlightDirection.RETURN, 2, "KIX", "ICN", "2026-09-05T17:00:00+09:00",
				"2026-09-05T19:00:00+09:00", "101"
		);

		ScheduleFlight flight = ScheduleFlight.create(
				null, "인천", "icn", "도쿄", "nrt", TripType.ROUND_TRIP,
				List.of(returnSecond, outbound, returnFirst)
		);

		assertThat(flight.provider()).isEqualTo("AVIATIONSTACK");
		assertThat(flight.originLocationCode()).isEqualTo("ICN");
		assertThat(flight.destinationLocationCode()).isEqualTo("NRT");
		assertThat(flight.tripType()).isEqualTo(TripType.ROUND_TRIP);
		assertThat(flight.segments()).containsExactly(outbound, returnFirst, returnSecond);
	}

	@Test
	void attachesOptionalFlightBundleToScheduleAggregate() {
		Schedule schedule = Schedule.create(
				new MemberUuid(UUID.randomUUID()), "도쿄 여행", "도쿄",
				LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5), new Headcount(1),
				ScheduleCost.zero(), null, null, null, null, ScheduleCreatorType.AI, List.of()
		);
		ScheduleFlight flight = ScheduleFlight.create(
				"AVIATIONSTACK", "인천", "ICN", "도쿄", "NRT", TripType.ONE_WAY,
				List.of(segment(
						FlightDirection.OUTBOUND, 1, "ICN", "NRT", "2026-09-01T09:00:00+09:00",
						"2026-09-01T11:30:00+09:00", "703"
				))
		);

		Schedule scheduleWithFlight = schedule.withFlight(flight);

		assertThat(schedule.flight()).isNull();
		assertThat(scheduleWithFlight.flight()).isSameAs(flight);
	}

	@Test
	void rejectsReturnSegmentForOneWayFlight() {
		ScheduleFlightSegment outbound = segment(
				FlightDirection.OUTBOUND, 1, "ICN", "NRT", "2026-09-01T09:00:00+09:00",
				"2026-09-01T11:30:00+09:00", "703"
		);
		ScheduleFlightSegment inbound = segment(
				FlightDirection.RETURN, 1, "NRT", "ICN", "2026-09-05T18:00:00+09:00",
				"2026-09-05T20:30:00+09:00", "704"
		);

		assertThatThrownBy(() -> ScheduleFlight.create(
				"AVIATIONSTACK", "인천", "ICN", "도쿄", "NRT", TripType.ONE_WAY,
				List.of(outbound, inbound)
		)).isInstanceOf(InvalidScheduleException.class)
				.hasMessage("One-way flight must not contain return segments.");
	}

	@Test
	void rejectsDiscontinuousStopoverSegments() {
		ScheduleFlightSegment first = segment(
				FlightDirection.OUTBOUND, 1, "ICN", "KIX", "2026-09-01T09:00:00+09:00",
				"2026-09-01T11:00:00+09:00", "100"
		);
		ScheduleFlightSegment second = segment(
				FlightDirection.OUTBOUND, 2, "FUK", "NRT", "2026-09-01T13:00:00+09:00",
				"2026-09-01T14:30:00+09:00", "101"
		);

		assertThatThrownBy(() -> ScheduleFlight.create(
				"AVIATIONSTACK", "인천", "ICN", "도쿄", "NRT", TripType.ONE_WAY,
				List.of(first, second)
		)).isInstanceOf(InvalidScheduleException.class)
				.hasMessage("OUTBOUND flight segments must form a continuous route.");
	}

	@Test
	void rejectsInvalidSegmentTimeAndOrder() {
		assertThatThrownBy(() -> ScheduleFlightSegment.create(
				FlightDirection.OUTBOUND, 0, "ICN", "NRT", null, null, null, null,
				OffsetDateTime.parse("2026-09-01T11:30:00+09:00"),
				OffsetDateTime.parse("2026-09-01T09:00:00+09:00"),
				"Asia/Seoul", "Asia/Tokyo", "KE", "703", null, null, null, null
		)).isInstanceOf(InvalidScheduleException.class)
				.hasMessage("Flight segment order must be at least 1.");
	}

	private ScheduleFlightSegment segment(
			FlightDirection direction,
			int order,
			String departure,
			String arrival,
			String departureAt,
			String arrivalAt,
			String flightNumber
	) {
		return ScheduleFlightSegment.create(
				direction, order, departure, arrival, "1", "2", "10", "20",
				OffsetDateTime.parse(departureAt), OffsetDateTime.parse(arrivalAt),
				"Asia/Seoul", "Asia/Tokyo", "KE", flightNumber, "KE", "B789", "scheduled", 150
		);
	}
}
