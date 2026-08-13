package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;

class FlightRecommendationPolicyTest {

	private final FlightRecommendationPolicy policy = new FlightRecommendationPolicy();

	@Test
	void recommendsTopThreeByDirectDurationDepartureAndArrivalTime() {
		FlightCandidate indirectButShort = candidate("GMP", "NRT", "100", 60, "08:00", "09:00");
		FlightCandidate laterDirect = candidate("ICN", "NRT", "101", 90, "12:00", "13:30");
		FlightCandidate earlierDirect = candidate("ICN", "NRT", "102", 90, "09:00", "10:30");
		FlightCandidate mediumDirect = candidate("ICN", "NRT", "103", 120, "10:00", "12:00");
		FlightCandidate longDirect = candidate("ICN", "NRT", "104", 180, "07:00", "10:00");

		List<FlightCandidate> result = policy.recommend(
				List.of(indirectButShort, laterDirect, earlierDirect, mediumDirect, longDirect),
				"ICN",
				"NRT"
		);

		assertThat(result).extracting(FlightCandidate::flightNumber)
				.containsExactly("102", "101", "103");
	}

	@Test
	void placesCandidatesWithMissingDurationAndTimeLast() {
		FlightCandidate complete = candidate("ICN", "NRT", "200", 120, "10:00", "12:00");
		FlightCandidate missing = new FlightCandidate(
				LocalDate.of(2026, 8, 13), "scheduled",
				new FlightCandidate.AirportSchedule("ICN", null, null, null, null),
				new FlightCandidate.AirportSchedule("NRT", null, null, null, null),
				"KE", "201", null, null, null
		);

		assertThat(policy.recommend(List.of(missing, complete), "ICN", "NRT"))
				.extracting(FlightCandidate::flightNumber)
				.containsExactly("200", "201");
	}

	private FlightCandidate candidate(
			String departure,
			String arrival,
			String flightNumber,
			long durationMinutes,
			String departureTime,
			String arrivalTime
	) {
		LocalDate date = LocalDate.of(2026, 8, 13);
		return new FlightCandidate(
				date, "scheduled",
				new FlightCandidate.AirportSchedule(
						departure, null, null, OffsetDateTime.parse(date + "T" + departureTime + ":00+09:00"),
						"Asia/Seoul"
				),
				new FlightCandidate.AirportSchedule(
						arrival, null, null, OffsetDateTime.parse(date + "T" + arrivalTime + ":00+09:00"),
						"Asia/Tokyo"
				),
				"KE", flightNumber, null, null, durationMinutes
		);
	}
}
