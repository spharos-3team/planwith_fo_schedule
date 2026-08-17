package com.planwith.planwith_fo_schedule.adapter.out.outbox;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_schedule.application.model.AiOperationType;
import com.planwith.planwith_fo_schedule.application.model.AiUsageReportEvent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_usage_outbox")
class AiUsageOutboxJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "outbox_id")
	private Long outboxId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "request_id", nullable = false, unique = true, length = 36)
	private UUID requestId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "operation_type", nullable = false, length = 20)
	private AiOperationType operationType;

	@Column(nullable = false, length = 500)
	private String model;

	@Column(name = "input_tokens", nullable = false)
	private long inputTokens;

	@Column(name = "output_tokens", nullable = false)
	private long outputTokens;

	@Column(name = "total_tokens", nullable = false)
	private long totalTokens;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AiUsageOutboxStatus status;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	@Column(name = "next_attempt_at", nullable = false)
	private Instant nextAttemptAt;

	@Column(name = "published_at")
	private Instant publishedAt;

	@Column(name = "last_error", length = 500)
	private String lastError;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected AiUsageOutboxJpaEntity() {
	}

	private AiUsageOutboxJpaEntity(AiUsageReportEvent event) {
		this.requestId = event.requestId();
		this.memberUuid = event.memberUuid();
		this.operationType = event.operationType();
		this.model = event.model();
		this.inputTokens = event.inputTokens();
		this.outputTokens = event.outputTokens();
		this.totalTokens = event.totalTokens();
		this.occurredAt = event.occurredAt();
		this.status = AiUsageOutboxStatus.PENDING;
		this.attemptCount = 0;
		this.nextAttemptAt = event.occurredAt();
		this.createdAt = Instant.now();
	}

	static AiUsageOutboxJpaEntity from(AiUsageReportEvent event) {
		return new AiUsageOutboxJpaEntity(event);
	}

	AiUsageReportEvent toEvent() {
		return new AiUsageReportEvent(
				memberUuid,
				requestId,
				operationType,
				model,
				inputTokens,
				outputTokens,
				totalTokens,
				occurredAt
		);
	}

	void markPublished(Instant publishedAt) {
		this.status = AiUsageOutboxStatus.PUBLISHED;
		this.publishedAt = publishedAt;
		this.lastError = null;
	}

	void scheduleRetry(Instant nextAttemptAt, String lastError) {
		this.attemptCount++;
		this.nextAttemptAt = nextAttemptAt;
		this.lastError = abbreviate(lastError);
	}

	private String abbreviate(String value) {
		if (value == null || value.length() <= 500) {
			return value;
		}
		return value.substring(0, 500);
	}

	UUID requestId() {
		return requestId;
	}

	AiUsageOutboxStatus status() {
		return status;
	}

	int attemptCount() {
		return attemptCount;
	}

	Instant nextAttemptAt() {
		return nextAttemptAt;
	}

	Instant publishedAt() {
		return publishedAt;
	}
}
