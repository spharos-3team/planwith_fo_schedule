package com.planwith.planwith_fo_schedule.application.model;

import java.time.LocalDate;

import com.planwith.planwith_fo_schedule.domain.TripType;

public record FlightRecommendationCacheKey(
		String departureAirportCode,
		String arrivalAirportCode,
		LocalDate departureDate,
		LocalDate returnDate,
		TripType tripType
) {
	public String value() {
		return String.join(":",
				nullSafe(departureAirportCode),
				nullSafe(arrivalAirportCode),
				nullSafe(departureDate),
				nullSafe(returnDate),
				nullSafe(tripType)
		);
	}

	private String nullSafe(Object value) {
		return value == null ? "none" : value.toString();
	}
}
