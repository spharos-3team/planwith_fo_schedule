package com.planwith.planwith_fo_schedule.domain;

import java.time.LocalTime;
import java.util.Objects;

import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleItemLocation;

public final class ScheduleItem {
	private static final int MAX_TITLE_LENGTH = 200;

	private final Long scheduleItemId;
	private final Long scheduleId;
	private final DayNumber day;
	private final ScheduleItemType itemType;
	private final String title;
	private final String content;
	private final ScheduleItemLocation location;
	private final LocalTime startTime;
	private final ScheduleCost expectedCost;

	private ScheduleItem(
			Long scheduleItemId,
			Long scheduleId,
			DayNumber day,
			ScheduleItemType itemType,
			String title,
			String content,
			ScheduleItemLocation location,
			LocalTime startTime,
			ScheduleCost expectedCost
	) {
		this.scheduleItemId = scheduleItemId;
		this.scheduleId = scheduleId;
		this.day = Objects.requireNonNull(day, "Schedule item day is required.");
		this.itemType = Objects.requireNonNull(itemType, "Schedule item type is required.");
		this.title = requireText(title, MAX_TITLE_LENGTH, "Schedule item title is required.");
		this.content = trimToNull(content);
		this.location = location;
		this.startTime = startTime;
		this.expectedCost = Objects.requireNonNull(expectedCost, "Schedule item expected cost is required.");
		if (!expectedCost.isSpecified()) {
			throw new InvalidScheduleException("Schedule item expected cost must be specified.");
		}
	}

	public static ScheduleItem create(
			DayNumber day,
			ScheduleItemType itemType,
			String title,
			String content,
			ScheduleItemLocation location,
			LocalTime startTime,
			ScheduleCost expectedCost
	) {
		return new ScheduleItem(
				null,
				null,
				day,
				itemType,
				title,
				content,
				location,
				startTime,
				expectedCost
		);
	}

	public static ScheduleItem restore(
			Long scheduleItemId,
			Long scheduleId,
			DayNumber day,
			ScheduleItemType itemType,
			String title,
			String content,
			ScheduleItemLocation location,
			LocalTime startTime,
			ScheduleCost expectedCost
	) {
		return new ScheduleItem(
				scheduleItemId,
				scheduleId,
				day,
				itemType,
				title,
				content,
				location,
				startTime,
				expectedCost
		);
	}

	private static String requireText(String value, int maxLength, String message) {
		String normalized = trimToNull(value);
		if (normalized == null) {
			throw new InvalidScheduleException(message);
		}
		if (normalized.length() > maxLength) {
			throw new InvalidScheduleException("Value must not exceed " + maxLength + " characters.");
		}
		return normalized;
	}

	private static String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	public Long scheduleItemId() { return scheduleItemId; }
	public Long scheduleId() { return scheduleId; }
	public DayNumber day() { return day; }
	public ScheduleItemType itemType() { return itemType; }
	public String title() { return title; }
	public String content() { return content; }
	public ScheduleItemLocation location() { return location; }
	public LocalTime startTime() { return startTime; }
	public ScheduleCost expectedCost() { return expectedCost; }
}
