package com.planwith.planwith_fo_schedule.application;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;

@Component
public class FlightRecommendationPolicy {

	private static final int RECOMMENDATION_LIMIT = 3;

	public List<FlightCandidate> recommend(
			List<FlightCandidate> candidates,
			String departureAirportCode,
			String arrivalAirportCode
	) {
		Comparator<FlightCandidate> comparator = Comparator
				.comparing((FlightCandidate candidate) -> !isDirect(
						candidate, departureAirportCode, arrivalAirportCode
				))
				.thenComparing(
						FlightCandidate::durationMinutes,
						Comparator.nullsLast(Comparator.naturalOrder())
				)
				.thenComparing(
						candidate -> scheduledAt(candidate.departure()),
						Comparator.nullsLast(Comparator.naturalOrder())
				)
				.thenComparing(
						candidate -> scheduledAt(candidate.arrival()),
						Comparator.nullsLast(Comparator.naturalOrder())
				);

		return candidates.stream()
				.sorted(comparator)
				.limit(RECOMMENDATION_LIMIT)
				.toList();
	}

	private boolean isDirect(
			FlightCandidate candidate,
			String departureAirportCode,
			String arrivalAirportCode
	) {
		return candidate.departure() != null
				&& candidate.arrival() != null
				&& departureAirportCode.equalsIgnoreCase(candidate.departure().airportCode())
				&& arrivalAirportCode.equalsIgnoreCase(candidate.arrival().airportCode());
	}

	private OffsetDateTime scheduledAt(FlightCandidate.AirportSchedule schedule) {
		return schedule == null ? null : schedule.scheduledAt();
	}
}
