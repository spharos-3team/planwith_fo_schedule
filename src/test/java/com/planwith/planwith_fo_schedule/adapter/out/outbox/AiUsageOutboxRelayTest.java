package com.planwith.planwith_fo_schedule.adapter.out.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.model.AiOperationType;
import com.planwith.planwith_fo_schedule.application.model.AiUsageReportEvent;
import com.planwith.planwith_fo_schedule.application.port.out.AiUsageEventPublisher;
import com.planwith.planwith_fo_schedule.config.AiUsageReportProperties;

class AiUsageOutboxRelayTest {

	private static final Instant NOW = Instant.parse("2026-08-17T05:00:00Z");

	private SpringDataAiUsageOutboxRepository repository;
	private AiUsageEventPublisher publisher;
	private AiUsageReportProperties properties;
	private AiUsageOutboxRelay relay;

	@BeforeEach
	void setUp() {
		repository = mock(SpringDataAiUsageOutboxRepository.class);
		publisher = mock(AiUsageEventPublisher.class);
		properties = new AiUsageReportProperties();
		properties.setRelayBatchSize(10);
		properties.setSendTimeout(Duration.ofSeconds(1));
		properties.setInitialRetryDelay(Duration.ofSeconds(5));
		properties.setMaxRetryDelay(Duration.ofMinutes(1));
		relay = new AiUsageOutboxRelay(
				repository,
				publisher,
				properties,
				Clock.fixed(NOW, ZoneOffset.UTC)
		);
	}

	@Test
	void marksOutboxPublishedAfterKafkaAcknowledgement() {
		AiUsageOutboxJpaEntity outbox = AiUsageOutboxJpaEntity.from(event());
		when(repository.findReadyToPublish(eq(AiUsageOutboxStatus.PENDING), eq(NOW), any()))
				.thenReturn(List.of(outbox));
		when(publisher.publish(outbox.toEvent())).thenReturn(CompletableFuture.completedFuture(null));

		relay.relayPendingEvents();

		verify(publisher).publish(outbox.toEvent());
		assertThat(outbox.status()).isEqualTo(AiUsageOutboxStatus.PUBLISHED);
		assertThat(outbox.publishedAt()).isEqualTo(NOW);
		assertThat(outbox.attemptCount()).isZero();
	}

	@Test
	void schedulesSameEventForRetryWhenKafkaPublishingFails() {
		AiUsageOutboxJpaEntity outbox = AiUsageOutboxJpaEntity.from(event());
		when(repository.findReadyToPublish(eq(AiUsageOutboxStatus.PENDING), eq(NOW), any()))
				.thenReturn(List.of(outbox));
		when(publisher.publish(outbox.toEvent()))
				.thenReturn(CompletableFuture.failedFuture(new IllegalStateException("Kafka unavailable")));

		relay.relayPendingEvents();

		assertThat(outbox.status()).isEqualTo(AiUsageOutboxStatus.PENDING);
		assertThat(outbox.attemptCount()).isEqualTo(1);
		assertThat(outbox.nextAttemptAt()).isEqualTo(NOW.plusSeconds(5));
		assertThat(outbox.toEvent().requestId()).isEqualTo(event().requestId());
	}

	private AiUsageReportEvent event() {
		return new AiUsageReportEvent(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				UUID.fromString("22222222-2222-2222-2222-222222222222"),
				AiOperationType.REGENERATE,
				"gpt-4o-mini",
				100,
				50,
				150,
				NOW
		);
	}
}
