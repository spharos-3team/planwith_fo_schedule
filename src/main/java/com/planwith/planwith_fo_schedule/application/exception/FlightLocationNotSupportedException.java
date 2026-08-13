package com.planwith.planwith_fo_schedule.application.exception;

public class FlightLocationNotSupportedException extends RuntimeException {

	private final String location;

	public FlightLocationNotSupportedException(String location) {
		super("Flight location is not supported.");
		this.location = location;
	}

	public String location() {
		return location;
	}
}
