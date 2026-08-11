package com.planwith.planwith_fo_schedule.application.exception;

public class AuthenticationRequiredException extends RuntimeException {

	public AuthenticationRequiredException() {
		super("Authentication is required.");
	}
}
