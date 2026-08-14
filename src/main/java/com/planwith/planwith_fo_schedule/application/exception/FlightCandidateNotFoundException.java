package com.planwith.planwith_fo_schedule.application.exception;

public class FlightCandidateNotFoundException extends RuntimeException {

	private final String carrierCode;
	private final String flightNumber;

	public FlightCandidateNotFoundException(String carrierCode, String flightNumber) {
		super("Selected flight candidate is no longer available.");
		this.carrierCode = carrierCode;
		this.flightNumber = flightNumber;
	}

	public String carrierCode() {
		return carrierCode;
	}

	public String flightNumber() {
		return flightNumber;
	}
}
