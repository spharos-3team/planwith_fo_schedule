package com.planwith.planwith_fo_schedule.application.exception;

public class FlightSearchException extends RuntimeException {

	private final String providerCode;

	public FlightSearchException(String message) {
		this(message, null, null);
	}

	public FlightSearchException(String message, Throwable cause) {
		this(message, null, cause);
	}

	public FlightSearchException(String message, String providerCode, Throwable cause) {
		super(message, cause);
		this.providerCode = providerCode;
	}

	public String providerCode() {
		return providerCode;
	}
}
