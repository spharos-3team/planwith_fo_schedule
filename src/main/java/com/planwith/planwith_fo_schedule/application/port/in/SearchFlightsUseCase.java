package com.planwith.planwith_fo_schedule.application.port.in;

import java.time.LocalDate;
import java.util.List;

import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.domain.FlightTripType;

public interface SearchFlightsUseCase {

	FlightSearchResult search(FlightSearchCommand command);

	record FlightSearchCommand(
			String departureAirportCode,
			String arrivalAirportCode,
			LocalDate departureDate,
			LocalDate returnDate,
			FlightTripType tripType
	) {
	}

	record FlightSearchResult(
			FlightTripType tripType,
			List<FlightCandidate> outboundCandidates,
			List<FlightCandidate> returnCandidates
	) {
		public FlightSearchResult {
			outboundCandidates = List.copyOf(outboundCandidates);
			returnCandidates = List.copyOf(returnCandidates);
		}
	}
}
