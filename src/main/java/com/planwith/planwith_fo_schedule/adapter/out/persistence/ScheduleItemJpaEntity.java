package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "schedule_items")
class ScheduleItemJpaEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "schedule_id", nullable = false)
	private ScheduleJpaEntity schedule;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(name = "starts_at", nullable = false)
	private Instant startsAt;

	@Column(name = "ends_at", nullable = false)
	private Instant endsAt;

	protected ScheduleItemJpaEntity() {
	}

	ScheduleItemJpaEntity(UUID id, String title, Instant startsAt, Instant endsAt) {
		this.id = id;
		this.title = title;
		this.startsAt = startsAt;
		this.endsAt = endsAt;
	}

	void assignSchedule(ScheduleJpaEntity schedule) {
		this.schedule = schedule;
	}

	UUID getId() {
		return id;
	}

	String getTitle() {
		return title;
	}

	Instant getStartsAt() {
		return startsAt;
	}

	Instant getEndsAt() {
		return endsAt;
	}
}
