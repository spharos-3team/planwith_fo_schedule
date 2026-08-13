package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.Objects;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSearchRequest;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase.FlightSearchCommand;

final class FlightSearchRequestMapper {

	private FlightSearchRequestMapper() {
	}

	static FlightSearchCommand toCommand(FlightSearchRequest request) {
		FlightSearchRequest validatedRequest = Objects.requireNonNull(request, "Flight search request is required.");
		return new FlightSearchCommand(
				validatedRequest.departureAirportCode(),
				validatedRequest.arrivalAirportCode(),
				validatedRequest.departureDate(),
				validatedRequest.returnDate(),
				validatedRequest.tripType()
		);
	}
}
