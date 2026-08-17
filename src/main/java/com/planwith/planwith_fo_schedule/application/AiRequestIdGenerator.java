package com.planwith.planwith_fo_schedule.application;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class AiRequestIdGenerator {

	public UUID generate() {
		return UUID.randomUUID();
	}
}
