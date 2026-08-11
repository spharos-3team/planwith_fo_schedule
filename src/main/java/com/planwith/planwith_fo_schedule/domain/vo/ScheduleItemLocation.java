package com.planwith.planwith_fo_schedule.domain.vo;

import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;

public record ScheduleItemLocation(
		String placeName,
		String placeAddress,
		GeoPoint coordinates
) {
	private static final int MAX_PLACE_NAME_LENGTH = 200;
	private static final int MAX_PLACE_ADDRESS_LENGTH = 500;

	public ScheduleItemLocation {
		placeName = normalize(placeName, MAX_PLACE_NAME_LENGTH, "Place name");
		placeAddress = normalize(placeAddress, MAX_PLACE_ADDRESS_LENGTH, "Place address");
		if (placeName == null && placeAddress == null && coordinates == null) {
			throw new InvalidScheduleException("Schedule item location must contain at least one value.");
		}
	}

	private static String normalize(String value, int maxLength, String fieldName) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String normalized = value.trim();
		if (normalized.length() > maxLength) {
			throw new InvalidScheduleException(fieldName + " must not exceed " + maxLength + " characters.");
		}
		return normalized;
	}
}
