package com.planwith.planwith_fo_schedule.application.exception;

public class AirportCodeNotFoundException extends RuntimeException {

	private final String location;

	public AirportCodeNotFoundException(String location) {
		super("No valid IATA airport code was found.");
		this.location = location;
	}

	public String location() {
		return location;
	}
}
