package com.planwith.planwith_fo_schedule.domain;

import java.time.LocalTime;
import java.util.Objects;

import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.GeoPoint;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;

public final class ScheduleItem {

	private final Long scheduleItemId;
	private final DayNumber dayNumber;
	private final LocalTime scheduleTime;
	private final String subtitle;
	private final ScheduleType scheduleType;
	private final String description;
	private final ScheduleCost estimatedCost;
	private final String placeName;
	private final String placeAddress;
	private final GeoPoint location;

	private ScheduleItem(
			Long scheduleItemId,
			DayNumber dayNumber,
			LocalTime scheduleTime,
			String subtitle,
			ScheduleType scheduleType,
			String description,
			ScheduleCost estimatedCost,
			String placeName,
			String placeAddress,
			GeoPoint location
	) {
		this.scheduleItemId = scheduleItemId;
		this.dayNumber = Objects.requireNonNull(dayNumber, "Day number is required.");
		this.scheduleTime = scheduleTime;
		this.subtitle = requireText(subtitle, 200, "Schedule item subtitle is required.");
		this.scheduleType = Objects.requireNonNull(scheduleType, "Schedule type is required.");
		this.description = trimToNull(description);
		this.estimatedCost = Objects.requireNonNull(estimatedCost, "Estimated cost is required.");
		if (!estimatedCost.isSpecified()) {
			throw new InvalidScheduleException("Estimated cost must be specified.");
		}
		this.placeName = requireOptionalText(placeName, 200, "Place name must not exceed 200 characters.");
		this.placeAddress = requireOptionalText(placeAddress, 500, "Place address must not exceed 500 characters.");
		this.location = location;
	}

	public static ScheduleItem create(
			DayNumber dayNumber,
			LocalTime scheduleTime,
			String subtitle,
			ScheduleType scheduleType,
			String description,
			ScheduleCost estimatedCost,
			String placeName,
			String placeAddress,
			GeoPoint location
	) {
		return new ScheduleItem(
				null,
				dayNumber,
				scheduleTime,
				subtitle,
				scheduleType,
				description,
				estimatedCost,
				placeName,
				placeAddress,
				location
		);
	}

	public static ScheduleItem restore(
			Long scheduleItemId,
			DayNumber dayNumber,
			LocalTime scheduleTime,
			String subtitle,
			ScheduleType scheduleType,
			String description,
			ScheduleCost estimatedCost,
			String placeName,
			String placeAddress,
			GeoPoint location
	) {
		return new ScheduleItem(
				scheduleItemId,
				dayNumber,
				scheduleTime,
				subtitle,
				scheduleType,
				description,
				estimatedCost,
				placeName,
				placeAddress,
				location
		);
	}

	private static String requireText(String value, int maxLength, String message) {
		String trimmed = trimToNull(value);
		if (trimmed == null) {
			throw new InvalidScheduleException(message);
		}
		if (trimmed.length() > maxLength) {
			throw new InvalidScheduleException("Value must not exceed " + maxLength + " characters.");
		}
		return trimmed;
	}

	private static String requireOptionalText(String value, int maxLength, String message) {
		String trimmed = trimToNull(value);
		if (trimmed != null && trimmed.length() > maxLength) {
			throw new InvalidScheduleException(message);
		}
		return trimmed;
	}

	private static String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	public Long scheduleItemId() { return scheduleItemId; }
	public DayNumber dayNumber() { return dayNumber; }
	public LocalTime scheduleTime() { return scheduleTime; }
	public String subtitle() { return subtitle; }
	public ScheduleType scheduleType() { return scheduleType; }
	public String description() { return description; }
	public ScheduleCost estimatedCost() { return estimatedCost; }
	public String placeName() { return placeName; }
	public String placeAddress() { return placeAddress; }
	public GeoPoint location() { return location; }
}
