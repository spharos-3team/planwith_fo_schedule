package com.planwith.planwith_fo_schedule.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.domain.FlightDirection;
import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;
import com.planwith.planwith_fo_schedule.domain.ScheduleFlight;
import com.planwith.planwith_fo_schedule.domain.ScheduleFlightSegment;
import com.planwith.planwith_fo_schedule.domain.TripType;

@Component
public class FlightDomainMapper {

	public ScheduleFlight toScheduleFlight(
			String departureLocation,
			String destinationLocation,
			TripType tripType,
			List<FlightCandidate> outboundCandidates,
			List<FlightCandidate> returnCandidates
	) {
		TripType requiredTripType = Objects.requireNonNull(tripType, "Flight trip type is required.");
		List<FlightCandidate> outbound = requiredCandidates(outboundCandidates, "Outbound flight candidates are required.");
		List<FlightCandidate> inbound = returnCandidates == null ? List.of() : List.copyOf(returnCandidates);

		if (requiredTripType == TripType.ROUND_TRIP && inbound.isEmpty()) {
			throw new InvalidScheduleException("Return flight candidates are required for a round trip.");
		}
		if (requiredTripType == TripType.ONE_WAY && !inbound.isEmpty()) {
			throw new InvalidScheduleException("One-way flight must not contain return candidates.");
		}

		List<ScheduleFlightSegment> segments = new ArrayList<>();
		segments.addAll(toSegments(outbound, FlightDirection.OUTBOUND));
		segments.addAll(toSegments(inbound, FlightDirection.RETURN));

		String originLocationCode = airportCode(outbound.get(0).departure(), "Outbound departure airport code is required.");
		String destinationLocationCode = airportCode(
				outbound.get(outbound.size() - 1).arrival(),
				"Outbound arrival airport code is required."
		);

		return ScheduleFlight.create(
				ScheduleFlight.DEFAULT_PROVIDER,
				departureLocation,
				originLocationCode,
				destinationLocation,
				destinationLocationCode,
				requiredTripType,
				segments
		);
	}

	private List<ScheduleFlightSegment> toSegments(
			List<FlightCandidate> candidates,
			FlightDirection direction
	) {
		List<ScheduleFlightSegment> segments = new ArrayList<>();
		for (int index = 0; index < candidates.size(); index++) {
			FlightCandidate candidate = Objects.requireNonNull(
					candidates.get(index),
					direction + " flight candidate is required."
			);
			segments.add(toSegment(candidate, direction, index + 1));
		}
		return segments;
	}

	private ScheduleFlightSegment toSegment(
			FlightCandidate candidate,
			FlightDirection direction,
			int segmentOrder
	) {
		FlightCandidate.AirportSchedule departure = Objects.requireNonNull(
				candidate.departure(),
				"Flight departure information is required."
		);
		FlightCandidate.AirportSchedule arrival = Objects.requireNonNull(
				candidate.arrival(),
				"Flight arrival information is required."
		);

		return ScheduleFlightSegment.create(
				direction,
				segmentOrder,
				departure.airportCode(),
				arrival.airportCode(),
				departure.terminal(),
				arrival.terminal(),
				departure.gate(),
				arrival.gate(),
				departure.scheduledAt(),
				arrival.scheduledAt(),
				departure.timezone(),
				arrival.timezone(),
				candidate.carrierCode(),
				candidate.flightNumber(),
				candidate.operatingCarrierCode(),
				candidate.aircraftCode(),
				candidate.flightStatus(),
				toDurationMinutes(candidate.durationMinutes())
		);
	}

	private List<FlightCandidate> requiredCandidates(List<FlightCandidate> candidates, String message) {
		if (candidates == null || candidates.isEmpty()) {
			throw new InvalidScheduleException(message);
		}
		return List.copyOf(candidates);
	}

	private String airportCode(FlightCandidate.AirportSchedule schedule, String message) {
		if (schedule == null || schedule.airportCode() == null || schedule.airportCode().isBlank()) {
			throw new InvalidScheduleException(message);
		}
		return schedule.airportCode();
	}

	private Integer toDurationMinutes(Long durationMinutes) {
		if (durationMinutes == null) {
			return null;
		}
		if (durationMinutes < 0 || durationMinutes > Integer.MAX_VALUE) {
			throw new InvalidScheduleException("Flight duration is out of range.");
		}
		return durationMinutes.intValue();
	}
}
