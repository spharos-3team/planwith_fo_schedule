package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.planwith.planwith_fo_schedule.domain.TripType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record FlightSearchRequest(
		@NotBlank
		@Pattern(regexp = "(?i)[A-Z]{3}", message = "departureAirportCode must be a three-letter IATA code")
		String departureAirportCode,
		@NotBlank
		@Pattern(regexp = "(?i)[A-Z]{3}", message = "arrivalAirportCode must be a three-letter IATA code")
		String arrivalAirportCode,
		@NotNull LocalDate departureDate,
		LocalDate returnDate,
		TripType tripType
) {
	public FlightSearchRequest {
		tripType = tripType == null ? TripType.ROUND_TRIP : tripType;
	}

	@JsonIgnore
	@AssertTrue(message = "returnDate is required for a round trip")
	public boolean isRoundTripReturnDatePresent() {
		return tripType != TripType.ROUND_TRIP || returnDate != null;
	}

	@JsonIgnore
	@AssertTrue(message = "returnDate must not be before departureDate")
	public boolean isFlightPeriodValid() {
		return departureDate == null || returnDate == null || !returnDate.isBefore(departureDate);
	}
}
