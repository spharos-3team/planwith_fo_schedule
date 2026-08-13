package com.planwith.planwith_fo_schedule.application.port.in;

import java.util.List;

public interface GetFlightLocationUseCase {

	FlightLocationResult getAirportCodes(String location);

	record FlightLocationResult(String location, List<String> airportCodes) {
		public FlightLocationResult {
			airportCodes = List.copyOf(airportCodes);
		}
	}
}
