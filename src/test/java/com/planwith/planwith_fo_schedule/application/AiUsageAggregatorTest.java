package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.model.AiOperationType;
import com.planwith.planwith_fo_schedule.application.model.OpenAiUsage;

class AiUsageAggregatorTest {

	private final AiUsageAggregator aggregator = new AiUsageAggregator();

	@Test
	void aggregatesMultipleOpenAiCallsIntoOneRequestUsage() {
		UUID memberUuid = UUID.randomUUID();
		UUID requestId = UUID.randomUUID();

		var result = aggregator.aggregate(
				memberUuid,
				requestId,
				AiOperationType.GENERATE,
				List.of(
						new OpenAiUsage("gpt-4o-mini-2024-07-18", 2_000, 3_000, 5_000),
						new OpenAiUsage("gpt-5.6-2026-08-01", 500, 200, 700)
				)
		);

		assertThat(result.memberUuid()).isEqualTo(memberUuid);
		assertThat(result.requestId()).isEqualTo(requestId);
		assertThat(result.operationType()).isEqualTo(AiOperationType.GENERATE);
		assertThat(result.model()).isEqualTo("gpt-4o-mini-2024-07-18,gpt-5.6-2026-08-01");
		assertThat(result.inputTokens()).isEqualTo(2_500);
		assertThat(result.outputTokens()).isEqualTo(3_200);
		assertThat(result.totalTokens()).isEqualTo(5_700);
	}

	@Test
	void keepsEachModelNameOnlyOnceInCallOrder() {
		var result = aggregator.aggregate(
				UUID.randomUUID(),
				UUID.randomUUID(),
				AiOperationType.REGENERATE,
				List.of(
						new OpenAiUsage("gpt-4o-mini", 10, 20, 30),
						new OpenAiUsage("gpt-4o-mini", 5, 10, 15)
				)
		);

		assertThat(result.model()).isEqualTo("gpt-4o-mini");
		assertThat(result.totalTokens()).isEqualTo(45);
	}

	@Test
	void rejectsAggregationWithoutUsage() {
		assertThatThrownBy(() -> aggregator.aggregate(
				UUID.randomUUID(),
				UUID.randomUUID(),
				AiOperationType.REVISE,
				List.of()
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("At least one");
	}

	@Test
	void rejectsTokenSumOverflow() {
		assertThatThrownBy(() -> aggregator.aggregate(
				UUID.randomUUID(),
				UUID.randomUUID(),
				AiOperationType.GENERATE,
				List.of(
						new OpenAiUsage("gpt-4o-mini", Long.MAX_VALUE, 0, Long.MAX_VALUE),
						new OpenAiUsage("gpt-5.6", 1, 0, 1)
				)
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("exceeds");
	}
}
