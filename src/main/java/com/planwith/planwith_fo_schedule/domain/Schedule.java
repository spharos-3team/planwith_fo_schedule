package com.planwith.planwith_fo_schedule.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

public final class Schedule {
	public static final String DEFAULT_CALENDAR_COLOR = "#4F46E5";

	private static final int MAX_TITLE_LENGTH = 200;
	private static final int MAX_DESTINATION_LENGTH = 200;
	private static final int MAX_CALENDAR_COLOR_LENGTH = 30;
	private static final String DEFAULT_TITLE_SUFFIX = " 여행";

	private final Long scheduleId;
	private final ScheduleUuid scheduleUuid;
	private final MemberUuid memberUuid;
	private final String title;
	private final String destination;
	private final SchedulePeriod period;
	private final Headcount headcount;
	private final ScheduleCost expectedCost;
	private final String transportation;
	private final String content;
	private final String calendarColor;
	private final CreatorType creatorType;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;
	private final LocalDateTime deletedAt;
	private final List<ScheduleItem> items;

	private Schedule(
			Long scheduleId,
			ScheduleUuid scheduleUuid,
			MemberUuid memberUuid,
			String title,
			String destination,
			SchedulePeriod period,
			Headcount headcount,
			ScheduleCost expectedCost,
			String transportation,
			String content,
			String calendarColor,
			CreatorType creatorType,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			LocalDateTime deletedAt,
			List<ScheduleItem> items
	) {
		this.scheduleId = scheduleId;
		this.scheduleUuid = Objects.requireNonNull(scheduleUuid, "Schedule UUID is required.");
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.destination = requireText(destination, MAX_DESTINATION_LENGTH, "Destination is required.");
		this.period = Objects.requireNonNull(period, "Schedule period is required.");
		this.title = resolveTitle(title, this.destination);
		this.headcount = Objects.requireNonNull(headcount, "Headcount is required.");
		this.expectedCost = Objects.requireNonNull(expectedCost, "Expected cost is required.");
		this.transportation = trimToNull(transportation);
		this.content = trimToNull(content);
		this.calendarColor = resolveCalendarColor(calendarColor);
		this.creatorType = Objects.requireNonNull(creatorType, "Creator type is required.");
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
		this.items = validateItems(items, period);
	}

	public static Schedule create(
			MemberUuid memberUuid,
			String title,
			String destination,
			LocalDate startDate,
			LocalDate endDate,
			Headcount headcount,
			ScheduleCost expectedCost,
			String transportation,
			String content,
			String calendarColor,
			CreatorType creatorType,
			List<ScheduleItem> items
	) {
		String validatedDestination = requireText(
				destination,
				MAX_DESTINATION_LENGTH,
				"Destination is required."
		);
		SchedulePeriod validatedPeriod = new SchedulePeriod(startDate, endDate);
		String resolvedTitle = resolveTitle(title, validatedDestination);
		String resolvedCalendarColor = resolveCalendarColor(calendarColor);
		CreatorType validatedCreatorType = Objects.requireNonNull(creatorType, "Creator type is required.");
		Headcount resolvedHeadcount = resolveHeadcount(headcount, validatedCreatorType);

		return new Schedule(
				null,
				ScheduleUuid.create(),
				memberUuid,
				resolvedTitle,
				validatedDestination,
				validatedPeriod,
				resolvedHeadcount,
				expectedCost,
				transportation,
				content,
				resolvedCalendarColor,
				validatedCreatorType,
				null,
				null,
				null,
				items
		);
	}

	public static Schedule restore(
			Long scheduleId,
			ScheduleUuid scheduleUuid,
			MemberUuid memberUuid,
			String title,
			String destination,
			SchedulePeriod period,
			Headcount headcount,
			ScheduleCost expectedCost,
			String transportation,
			String content,
			String calendarColor,
			CreatorType creatorType,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			LocalDateTime deletedAt,
			List<ScheduleItem> items
	) {
		return new Schedule(
				scheduleId,
				scheduleUuid,
				memberUuid,
				title,
				destination,
				period,
				headcount,
				expectedCost,
				transportation,
				content,
				calendarColor,
				creatorType,
				createdAt,
				updatedAt,
				deletedAt,
				items
		);
	}

	public Schedule update(
			String title,
			String destination,
			LocalDate startDate,
			LocalDate endDate,
			Headcount headcount,
			ScheduleCost expectedCost,
			String transportation,
			String content,
			String calendarColor
	) {
		String updatedDestination = destination == null
				? this.destination
				: requireText(destination, MAX_DESTINATION_LENGTH, "Destination is required.");
		SchedulePeriod updatedPeriod = new SchedulePeriod(
				startDate == null ? period.startDate() : startDate,
				endDate == null ? period.endDate() : endDate
		);

		return new Schedule(
				scheduleId,
				scheduleUuid,
				memberUuid,
				title == null ? this.title : resolveTitle(title, updatedDestination),
				updatedDestination,
				updatedPeriod,
				headcount == null ? this.headcount : headcount,
				expectedCost == null ? this.expectedCost : expectedCost,
				transportation == null ? this.transportation : transportation,
				content == null ? this.content : content,
				calendarColor == null ? this.calendarColor : calendarColor,
				creatorType,
				createdAt,
				updatedAt,
				deletedAt,
				items
		);
	}

	public Schedule delete(LocalDateTime deletionTime) {
		return new Schedule(
				scheduleId,
				scheduleUuid,
				memberUuid,
				title,
				destination,
				period,
				headcount,
				expectedCost,
				transportation,
				content,
				calendarColor,
				creatorType,
				createdAt,
				updatedAt,
				Objects.requireNonNull(deletionTime, "Deletion time is required."),
				items
		);
	}

	private static List<ScheduleItem> validateItems(List<ScheduleItem> items, SchedulePeriod period) {
		List<ScheduleItem> copiedItems = List.copyOf(Objects.requireNonNull(items, "Schedule items are required."));
		for (ScheduleItem item : copiedItems) {
			if (item.day().value() > period.numberOfDays()) {
				throw new InvalidScheduleException("Schedule item day must be within the schedule period.");
			}
		}
		return copiedItems;
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

	private static String resolveTitle(String title, String destination) {
		String trimmedTitle = trimToNull(title);
		if (trimmedTitle != null) {
			return requireText(trimmedTitle, MAX_TITLE_LENGTH, "Schedule title is required.");
		}

		String automaticTitle = destination + DEFAULT_TITLE_SUFFIX;
		return automaticTitle.length() <= MAX_TITLE_LENGTH
				? automaticTitle
				: automaticTitle.substring(0, MAX_TITLE_LENGTH);
	}

	private static String resolveCalendarColor(String calendarColor) {
		String trimmedColor = trimToNull(calendarColor);
		if (trimmedColor == null) {
			return DEFAULT_CALENDAR_COLOR;
		}
		return requireOptionalText(
				trimmedColor,
				MAX_CALENDAR_COLOR_LENGTH,
				"Calendar color must not exceed 30 characters."
		);
	}

	private static Headcount resolveHeadcount(Headcount headcount, CreatorType creatorType) {
		if (headcount != null) {
			return headcount;
		}
		if (creatorType == CreatorType.AI) {
			return Headcount.defaultValue();
		}
		throw new InvalidScheduleException("Headcount is required for non-AI schedules.");
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

	public Long scheduleId() { return scheduleId; }
	public ScheduleUuid scheduleUuid() { return scheduleUuid; }
	public MemberUuid memberUuid() { return memberUuid; }
	public String title() { return title; }
	public String destination() { return destination; }
	public SchedulePeriod period() { return period; }
	public Headcount headcount() { return headcount; }
	public ScheduleCost expectedCost() { return expectedCost; }
	public String transportation() { return transportation; }
	public String content() { return content; }
	public String calendarColor() { return calendarColor; }
	public CreatorType creatorType() { return creatorType; }
	public LocalDateTime createdAt() { return createdAt; }
	public LocalDateTime updatedAt() { return updatedAt; }
	public LocalDateTime deletedAt() { return deletedAt; }
	public List<ScheduleItem> items() { return items; }
}
