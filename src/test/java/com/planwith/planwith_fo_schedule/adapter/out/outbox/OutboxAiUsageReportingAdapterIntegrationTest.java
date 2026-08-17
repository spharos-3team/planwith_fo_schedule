package com.planwith.planwith_fo_schedule.adapter.out.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.model.AiOperationType;
import com.planwith.planwith_fo_schedule.application.model.AiUsageReportEvent;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OutboxAiUsageReportingAdapterIntegrationTest {

	@Autowired
	private OutboxAiUsageReportingAdapter adapter;

	@Autowired
	private SpringDataAiUsageOutboxRepository repository;

	@BeforeEach
	void clearOutbox() {
		repository.deleteAll();
	}

	@Test
	void storesOnlyOneOutboxRecordForSameRequestId() {
		UUID requestId = UUID.randomUUID();
		AiUsageReportEvent event = event(requestId);

		adapter.report(event);
		adapter.report(event);

		assertThat(repository.findAll()).singleElement().satisfies(outbox -> {
			assertThat(outbox.requestId()).isEqualTo(requestId);
			assertThat(outbox.status()).isEqualTo(AiUsageOutboxStatus.PENDING);
			assertThat(outbox.attemptCount()).isZero();
			assertThat(outbox.toEvent()).isEqualTo(event);
		});
		assertThat(repository.findReadyToPublish(
				AiUsageOutboxStatus.PENDING,
				Instant.parse("2026-08-18T00:00:00Z"),
				PageRequest.of(0, 10)
		)).hasSize(1);
	}

	private AiUsageReportEvent event(UUID requestId) {
		return new AiUsageReportEvent(
				UUID.randomUUID(),
				requestId,
				AiOperationType.GENERATE,
				"gpt-4o-mini",
				100,
				50,
				150,
				Instant.parse("2026-08-17T05:00:00Z")
		);
	}
}
