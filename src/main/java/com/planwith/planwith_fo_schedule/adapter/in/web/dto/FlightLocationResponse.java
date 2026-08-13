package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.util.List;

public record FlightLocationResponse(String location, List<String> airportCodes) {

	public FlightLocationResponse {
		airportCodes = List.copyOf(airportCodes);
	}
}
