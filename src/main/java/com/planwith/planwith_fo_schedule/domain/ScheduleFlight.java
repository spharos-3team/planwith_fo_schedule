package com.planwith.planwith_fo_schedule.domain;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ScheduleFlight {

	public static final String DEFAULT_PROVIDER = "AVIATIONSTACK";

	private final Long scheduleFlightId;
	private final Long scheduleId;
	private final String provider;
	private final String departureLocation;
	private final String originLocationCode;
	private final String destinationLocation;
	private final String destinationLocationCode;
	private final TripType tripType;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;
	private final List<ScheduleFlightSegment> segments;

	private ScheduleFlight(
			Long scheduleFlightId,
			Long scheduleId,
			String provider,
			String departureLocation,
			String originLocationCode,
			String destinationLocation,
			String destinationLocationCode,
			TripType tripType,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			List<ScheduleFlightSegment> segments
	) {
		this.scheduleFlightId = scheduleFlightId;
		this.scheduleId = scheduleId;
		this.provider = resolveProvider(provider);
		this.departureLocation = requireText(departureLocation, 200, "Flight departure location is required.");
		this.originLocationCode = requireIata(originLocationCode);
		this.destinationLocation = requireText(destinationLocation, 200, "Flight destination location is required.");
		this.destinationLocationCode = requireIata(destinationLocationCode);
		if (this.originLocationCode.equals(this.destinationLocationCode)) {
			throw new InvalidScheduleException("Flight origin and destination must be different.");
		}
		this.tripType = Objects.requireNonNull(tripType, "Flight trip type is required.");
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.segments = validateSegments(segments);
		validateSegmentOwnership(this.segments);
	}

	public static ScheduleFlight create(
			String provider,
			String departureLocation,
			String originLocationCode,
			String destinationLocation,
			String destinationLocationCode,
			TripType tripType,
			List<ScheduleFlightSegment> segments
	) {
		return new ScheduleFlight(
				null, null, provider, departureLocation, originLocationCode, destinationLocation,
				destinationLocationCode, tripType, null, null, segments
		);
	}

	public static ScheduleFlight restore(
			Long scheduleFlightId,
			Long scheduleId,
			String provider,
			String departureLocation,
			String originLocationCode,
			String destinationLocation,
			String destinationLocationCode,
			TripType tripType,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			List<ScheduleFlightSegment> segments
	) {
		return new ScheduleFlight(
				scheduleFlightId, scheduleId, provider, departureLocation, originLocationCode,
				destinationLocation, destinationLocationCode, tripType, createdAt, updatedAt, segments
		);
	}

	private List<ScheduleFlightSegment> validateSegments(List<ScheduleFlightSegment> segments) {
		List<ScheduleFlightSegment> copied = List.copyOf(Objects.requireNonNull(segments, "Flight segments are required."));
		if (copied.isEmpty()) {
			throw new InvalidScheduleException("At least one flight segment is required.");
		}
		validateDirection(copied, FlightDirection.OUTBOUND, originLocationCode, destinationLocationCode);
		if (tripType == TripType.ROUND_TRIP) {
			validateDirection(copied, FlightDirection.RETURN, destinationLocationCode, originLocationCode);
			validateReturnAfterOutbound(copied);
		} else if (copied.stream().anyMatch(segment -> segment.direction() == FlightDirection.RETURN)) {
			throw new InvalidScheduleException("One-way flight must not contain return segments.");
		}
		return copied.stream()
				.sorted(Comparator.comparing(ScheduleFlightSegment::direction)
						.thenComparingInt(ScheduleFlightSegment::segmentOrder))
				.toList();
	}

	private void validateSegmentOwnership(List<ScheduleFlightSegment> segments) {
		if (scheduleFlightId == null) {
			return;
		}
		boolean mismatched = segments.stream()
				.anyMatch(segment -> segment.scheduleFlightId() != null
						&& !scheduleFlightId.equals(segment.scheduleFlightId()));
		if (mismatched) {
			throw new InvalidScheduleException("Flight segment belongs to another schedule flight.");
		}
	}

	private void validateReturnAfterOutbound(List<ScheduleFlightSegment> segments) {
		ScheduleFlightSegment lastOutbound = segments.stream()
				.filter(segment -> segment.direction() == FlightDirection.OUTBOUND)
				.max(Comparator.comparingInt(ScheduleFlightSegment::segmentOrder))
				.orElseThrow();
		ScheduleFlightSegment firstReturn = segments.stream()
				.filter(segment -> segment.direction() == FlightDirection.RETURN)
				.min(Comparator.comparingInt(ScheduleFlightSegment::segmentOrder))
				.orElseThrow();
		if (firstReturn.departureAt().isBefore(lastOutbound.arrivalAt())) {
			throw new InvalidScheduleException("Return flight must depart after outbound arrival.");
		}
	}

	private void validateDirection(
			List<ScheduleFlightSegment> segments,
			FlightDirection direction,
			String expectedOrigin,
			String expectedDestination
	) {
		List<ScheduleFlightSegment> directional = segments.stream()
				.filter(segment -> segment.direction() == direction)
				.sorted(Comparator.comparingInt(ScheduleFlightSegment::segmentOrder))
				.toList();
		if (directional.isEmpty()) {
			throw new InvalidScheduleException(direction + " flight segment is required.");
		}
		for (int index = 0; index < directional.size(); index++) {
			ScheduleFlightSegment current = directional.get(index);
			if (current.segmentOrder() != index + 1) {
				throw new InvalidScheduleException(direction + " segment orders must start at 1 and be consecutive.");
			}
			if (index > 0) {
				ScheduleFlightSegment previous = directional.get(index - 1);
				if (!previous.arrivalAirportCode().equals(current.departureAirportCode())) {
					throw new InvalidScheduleException(direction + " flight segments must form a continuous route.");
				}
				if (current.departureAt().isBefore(previous.arrivalAt())) {
					throw new InvalidScheduleException(direction + " flight segments must be chronological.");
				}
			}
		}
		if (!directional.get(0).departureAirportCode().equals(expectedOrigin)
				|| !directional.get(directional.size() - 1).arrivalAirportCode().equals(expectedDestination)) {
			throw new InvalidScheduleException(direction + " flight route does not match the schedule flight route.");
		}
	}

	private static String resolveProvider(String provider) {
		String value = provider == null || provider.isBlank() ? DEFAULT_PROVIDER : provider.trim();
		if (value.length() > 30) {
			throw new InvalidScheduleException("Flight provider must not exceed 30 characters.");
		}
		return value.toUpperCase(Locale.ROOT);
	}

	private static String requireIata(String value) {
		String code = requireText(value, 3, "Airport IATA code is required.").toUpperCase(Locale.ROOT);
		if (!code.matches("[A-Z]{3}")) {
			throw new InvalidScheduleException("Airport IATA code must contain three letters.");
		}
		return code;
	}

	private static String requireText(String value, int maxLength, String message) {
		if (value == null || value.isBlank()) {
			throw new InvalidScheduleException(message);
		}
		String trimmed = value.trim();
		if (trimmed.length() > maxLength) {
			throw new InvalidScheduleException(message + " Maximum length is " + maxLength + ".");
		}
		return trimmed;
	}

	public Long scheduleFlightId() { return scheduleFlightId; }
	public Long scheduleId() { return scheduleId; }
	public String provider() { return provider; }
	public String departureLocation() { return departureLocation; }
	public String originLocationCode() { return originLocationCode; }
	public String destinationLocation() { return destinationLocation; }
	public String destinationLocationCode() { return destinationLocationCode; }
	public TripType tripType() { return tripType; }
	public LocalDateTime createdAt() { return createdAt; }
	public LocalDateTime updatedAt() { return updatedAt; }
	public List<ScheduleFlightSegment> segments() { return segments; }
}
