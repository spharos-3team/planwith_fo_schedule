package com.planwith.planwith_fo_schedule.adapter.out.flight;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
record AviationStackFlightsResponse(List<FlightData> data, ApiError error) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	record FlightData(
			@JsonProperty("flight_date") String flightDate,
			@JsonProperty("flight_status") String flightStatus,
			AirportEndpoint departure,
			AirportEndpoint arrival,
			Airline airline,
			Flight flight,
			Aircraft aircraft
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record AirportEndpoint(
			String iata,
			String terminal,
			String gate,
			String scheduled,
			String timezone
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Airline(String iata) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Flight(String number, Codeshared codeshared) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Codeshared(@JsonProperty("airline_iata") String airlineIata) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Aircraft(String iata) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record ApiError(Object code, String message) {
	}
}
