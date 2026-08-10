package com.planwith.planwith_fo_schedule.domain;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Schedule {

	private final UUID id;
	private final UUID ownerId;
	private final String title;
	private final List<ScheduleItem> items;

	private Schedule(UUID id, UUID ownerId, String title, List<ScheduleItem> items) {
		this.id = Objects.requireNonNull(id, "Schedule ID is required.");
		this.ownerId = Objects.requireNonNull(ownerId, "Owner ID is required.");
		this.title = requireText(title, "Schedule title is required.");
		this.items = List.copyOf(Objects.requireNonNull(items, "Schedule items are required."));
	}

	public static Schedule create(UUID ownerId, String title, List<ScheduleItem> items) {
		return new Schedule(UUID.randomUUID(), ownerId, title, items);
	}

	public static Schedule restore(UUID id, UUID ownerId, String title, List<ScheduleItem> items) {
		return new Schedule(id, ownerId, title, items);
	}

	private static String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new InvalidScheduleException(message);
		}
		return value.trim();
	}

	public UUID id() {
		return id;
	}

	public UUID ownerId() {
		return ownerId;
	}

	public String title() {
		return title;
	}

	public List<ScheduleItem> items() {
		return items;
	}
}
