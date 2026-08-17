package com.planwith.planwith_fo_schedule.adapter.out.outbox;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.port.out.AiUsageEventPublisher;
import com.planwith.planwith_fo_schedule.config.AiUsageReportProperties;

@Component
@ConditionalOnProperty(name = "ai.usage-report.enabled", havingValue = "true")
public class AiUsageOutboxRelay {

	private static final Logger log = LoggerFactory.getLogger(AiUsageOutboxRelay.class);

	private final SpringDataAiUsageOutboxRepository repository;
	private final AiUsageEventPublisher publisher;
	private final AiUsageReportProperties properties;
	private final Clock clock;

	public AiUsageOutboxRelay(
			SpringDataAiUsageOutboxRepository repository,
			AiUsageEventPublisher publisher,
			AiUsageReportProperties properties
	) {
		this(repository, publisher, properties, Clock.systemUTC());
	}

	AiUsageOutboxRelay(
			SpringDataAiUsageOutboxRepository repository,
			AiUsageEventPublisher publisher,
			AiUsageReportProperties properties,
			Clock clock
	) {
		this.repository = repository;
		this.publisher = publisher;
		this.properties = properties;
		this.clock = clock;
	}

	@Scheduled(
			fixedDelayString = "${ai.usage-report.relay-interval:5s}",
			initialDelayString = "${ai.usage-report.relay-initial-delay:5s}"
	)
	@Transactional
	public void relayPendingEvents() {
		Instant now = Instant.now(clock);
		List<AiUsageOutboxJpaEntity> pendingEvents = repository.findReadyToPublish(
				AiUsageOutboxStatus.PENDING,
				now,
				PageRequest.of(0, validBatchSize())
		);
		for (AiUsageOutboxJpaEntity outbox : pendingEvents) {
			publish(outbox);
		}
	}

	private void publish(AiUsageOutboxJpaEntity outbox) {
		try {
			publisher.publish(outbox.toEvent())
					.get(validSendTimeout().toMillis(), TimeUnit.MILLISECONDS);
			Instant publishedAt = Instant.now(clock);
			outbox.markPublished(publishedAt);
			log.info("AiUsageOutboxRelay : publish : AI 사용량 Outbox 발행 완료 - requestId={}, "
							+ "attemptCount={}",
					outbox.requestId(), outbox.attemptCount() + 1);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			scheduleRetry(outbox, exception);
		} catch (ExecutionException | TimeoutException | RuntimeException exception) {
			scheduleRetry(outbox, exception);
		}
	}

	private void scheduleRetry(AiUsageOutboxJpaEntity outbox, Exception exception) {
		Duration retryDelay = retryDelay(outbox.attemptCount());
		Instant nextAttemptAt = Instant.now(clock).plus(retryDelay);
		outbox.scheduleRetry(nextAttemptAt, rootMessage(exception));
		log.warn("AiUsageOutboxRelay : scheduleRetry : AI 사용량 Outbox 발행 실패로 재전송 예약 - "
						+ "requestId={}, attemptCount={}, nextAttemptAt={}, exceptionType={}",
				outbox.requestId(), outbox.attemptCount(), nextAttemptAt,
				rootCause(exception).getClass().getSimpleName());
	}

	private Duration retryDelay(int attemptCount) {
		long initialMillis = validInitialRetryDelay().toMillis();
		long maxMillis = validMaxRetryDelay().toMillis();
		long multiplier = 1L << Math.min(attemptCount, 30);
		try {
			return Duration.ofMillis(Math.min(Math.multiplyExact(initialMillis, multiplier), maxMillis));
		} catch (ArithmeticException exception) {
			return Duration.ofMillis(maxMillis);
		}
	}

	private int validBatchSize() {
		return properties.getRelayBatchSize() > 0 ? properties.getRelayBatchSize() : 50;
	}

	private Duration validSendTimeout() {
		return positiveOrDefault(properties.getSendTimeout(), Duration.ofSeconds(10));
	}

	private Duration validInitialRetryDelay() {
		return positiveOrDefault(properties.getInitialRetryDelay(), Duration.ofSeconds(5));
	}

	private Duration validMaxRetryDelay() {
		Duration initial = validInitialRetryDelay();
		Duration maximum = positiveOrDefault(properties.getMaxRetryDelay(), Duration.ofMinutes(5));
		return maximum.compareTo(initial) < 0 ? initial : maximum;
	}

	private Duration positiveOrDefault(Duration value, Duration defaultValue) {
		return value == null || value.isZero() || value.isNegative() ? defaultValue : value;
	}

	private String rootMessage(Exception exception) {
		Throwable cause = rootCause(exception);
		return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
	}

	private Throwable rootCause(Throwable throwable) {
		Throwable current = throwable;
		while (current.getCause() != null) {
			current = current.getCause();
		}
		return current;
	}
}
