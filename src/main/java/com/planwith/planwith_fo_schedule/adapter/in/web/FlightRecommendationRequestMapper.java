package com.planwith.planwith_fo_schedule.adapter.in.web;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSearchRequest;
import com.planwith.planwith_fo_schedule.application.port.in.RecommendFlightsUseCase.FlightRecommendationCommand;

final class FlightRecommendationRequestMapper {

	private FlightRecommendationRequestMapper() {
	}

	static FlightRecommendationCommand toCommand(FlightSearchRequest request) {
		return new FlightRecommendationCommand(
				request.departureAirportCode(),
				request.arrivalAirportCode(),
				request.departureDate(),
				request.returnDate(),
				request.tripType()
		);
	}
}
