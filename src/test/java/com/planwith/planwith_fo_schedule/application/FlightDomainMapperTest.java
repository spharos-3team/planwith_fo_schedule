package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate.AirportSchedule;
import com.planwith.planwith_fo_schedule.domain.FlightDirection;
import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;
import com.planwith.planwith_fo_schedule.domain.ScheduleFlight;
import com.planwith.planwith_fo_schedule.domain.TripType;

class FlightDomainMapperTest {

	private final FlightDomainMapper mapper = new FlightDomainMapper();

	@Test
	void convertsDirectOneWayCandidateToScheduleFlight() {
		FlightCandidate outbound = candidate(
				"ICN", "NRT", "2026-09-01T09:00:00+09:00", "2026-09-01T11:30:00+09:00", "703"
		);

		ScheduleFlight flight = mapper.toScheduleFlight(
				"Seoul", "Tokyo", TripType.ONE_WAY, List.of(outbound), List.of()
		);

		assertThat(flight.provider()).isEqualTo("AVIATIONSTACK");
		assertThat(flight.originLocationCode()).isEqualTo("ICN");
		assertThat(flight.destinationLocationCode()).isEqualTo("NRT");
		assertThat(flight.tripType()).isEqualTo(TripType.ONE_WAY);
		assertThat(flight.segments()).hasSize(1);
		assertThat(flight.segments().get(0).direction()).isEqualTo(FlightDirection.OUTBOUND);
		assertThat(flight.segments().get(0).segmentOrder()).isEqualTo(1);
		assertThat(flight.segments().get(0).departureGate()).isEqualTo("10");
		assertThat(flight.segments().get(0).arrivalGate()).isEqualTo("20");
	}

	@Test
	void convertsStopoverOutboundAndReturnCandidatesInOrder() {
		FlightCandidate outboundFirst = candidate(
				"ICN", "HKG", "2026-09-01T09:00:00+09:00", "2026-09-01T12:00:00+08:00", "601"
		);
		FlightCandidate outboundSecond = candidate(
				"HKG", "CDG", "2026-09-01T14:00:00+08:00", "2026-09-01T21:00:00+02:00", "285"
		);
		FlightCandidate inbound = candidate(
				"CDG", "ICN", "2026-09-07T13:00:00+02:00", "2026-09-08T08:00:00+09:00", "902"
		);

		ScheduleFlight flight = mapper.toScheduleFlight(
				"Seoul", "Paris", TripType.ROUND_TRIP,
				List.of(outboundFirst, outboundSecond), List.of(inbound)
		);

		assertThat(flight.originLocationCode()).isEqualTo("ICN");
		assertThat(flight.destinationLocationCode()).isEqualTo("CDG");
		assertThat(flight.segments())
				.extracting(segment -> segment.direction() + ":" + segment.segmentOrder())
				.containsExactly("OUTBOUND:1", "OUTBOUND:2", "RETURN:1");
		assertThat(flight.segments().get(1).departureAirportCode()).isEqualTo("HKG");
		assertThat(flight.segments().get(1).arrivalAirportCode()).isEqualTo("CDG");
	}

	@Test
	void rejectsRoundTripWithoutReturnCandidate() {
		FlightCandidate outbound = candidate(
				"ICN", "NRT", "2026-09-01T09:00:00+09:00", "2026-09-01T11:30:00+09:00", "703"
		);

		assertThatThrownBy(() -> mapper.toScheduleFlight(
				"Seoul", "Tokyo", TripType.ROUND_TRIP, List.of(outbound), List.of()
		))
				.isInstanceOf(InvalidScheduleException.class)
				.hasMessage("Return flight candidates are required for a round trip.");
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
				departureTime.toLocalDate(),
				"scheduled",
				new AirportSchedule(departureCode, "1", "10", departureTime, timezone(departureCode)),
				new AirportSchedule(arrivalCode, "2", "20", arrivalTime, timezone(arrivalCode)),
				"KE",
				flightNumber,
				"KE",
				"B789",
				java.time.Duration.between(departureTime, arrivalTime).toMinutes()
		);
	}

	private String timezone(String airportCode) {
		return switch (airportCode) {
			case "ICN" -> "Asia/Seoul";
			case "HKG" -> "Asia/Hong_Kong";
			case "NRT" -> "Asia/Tokyo";
			case "CDG" -> "Europe/Paris";
			default -> "UTC";
		};
	}
}
