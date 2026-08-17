package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.model.AiOperationType;
import com.planwith.planwith_fo_schedule.application.model.AiUsageResult;

class AiUsageReportEventFactoryTest {

	@Test
	void convertsAggregatedUsageToTokenServiceEvent() {
		Instant occurredAt = Instant.parse("2026-08-17T05:00:00Z");
		AiUsageReportEventFactory factory = new AiUsageReportEventFactory(
				Clock.fixed(occurredAt, ZoneOffset.UTC)
		);
		UUID memberUuid = UUID.randomUUID();
		UUID requestId = UUID.randomUUID();
		AiUsageResult usage = new AiUsageResult(
				memberUuid,
				requestId,
				AiOperationType.GENERATE,
				"gpt-4o-mini,gpt-5.6",
				2_500,
				3_200,
				5_700
		);

		var event = factory.create(usage);

		assertThat(event.memberUuid()).isEqualTo(memberUuid);
		assertThat(event.requestId()).isEqualTo(requestId);
		assertThat(event.operationType()).isEqualTo(AiOperationType.GENERATE);
		assertThat(event.model()).isEqualTo("gpt-4o-mini,gpt-5.6");
		assertThat(event.inputTokens()).isEqualTo(2_500);
		assertThat(event.outputTokens()).isEqualTo(3_200);
		assertThat(event.totalTokens()).isEqualTo(5_700);
		assertThat(event.occurredAt()).isEqualTo(occurredAt);
	}
}
