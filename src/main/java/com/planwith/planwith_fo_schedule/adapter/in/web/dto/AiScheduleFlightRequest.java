package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import com.planwith.planwith_fo_schedule.domain.FlightTravelClass;
import com.planwith.planwith_fo_schedule.domain.FlightTripType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AiScheduleFlightRequest(
		@NotBlank @Size(max = 200) String departureLocation,
		@NotBlank
		@Pattern(regexp = "[A-Z]{3}", message = "originLocationCode must be a three-letter IATA code")
		String originLocationCode,
		@NotBlank
		@Pattern(regexp = "[A-Z]{3}", message = "destinationLocationCode must be a three-letter IATA code")
		String destinationLocationCode,
		FlightTripType tripType,
		FlightTravelClass travelClass
) {
}
