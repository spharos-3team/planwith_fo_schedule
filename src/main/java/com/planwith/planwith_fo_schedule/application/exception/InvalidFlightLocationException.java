package com.planwith.planwith_fo_schedule.application.exception;

public class InvalidFlightLocationException extends RuntimeException {

	public InvalidFlightLocationException() {
		super("Flight location is required.");
	}
}
