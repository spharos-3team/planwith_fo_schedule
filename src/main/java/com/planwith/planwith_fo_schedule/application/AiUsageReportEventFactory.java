package com.planwith.planwith_fo_schedule.application;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_schedule.application.model.AiUsageReportEvent;
import com.planwith.planwith_fo_schedule.application.model.AiUsageResult;

@Component
public class AiUsageReportEventFactory {

	private final Clock clock;

	public AiUsageReportEventFactory() {
		this(Clock.systemUTC());
	}

	AiUsageReportEventFactory(Clock clock) {
		this.clock = clock;
	}

	public AiUsageReportEvent create(AiUsageResult usage) {
		return new AiUsageReportEvent(
				usage.memberUuid(),
				usage.requestId(),
				usage.operationType(),
				usage.model(),
				usage.inputTokens(),
				usage.outputTokens(),
				usage.totalTokens(),
				Instant.now(clock)
		);
	}
}
