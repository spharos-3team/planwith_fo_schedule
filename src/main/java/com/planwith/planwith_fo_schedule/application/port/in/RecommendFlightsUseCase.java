package com.planwith.planwith_fo_schedule.application.port.in;

import java.time.LocalDate;

import com.planwith.planwith_fo_schedule.application.model.FlightRecommendation;
import com.planwith.planwith_fo_schedule.domain.TripType;

public interface RecommendFlightsUseCase {

	FlightRecommendation recommend(FlightRecommendationCommand command);

	record FlightRecommendationCommand(
			String departureAirportCode,
			String arrivalAirportCode,
			LocalDate departureDate,
			LocalDate returnDate,
			TripType tripType
	) {
	}
}
