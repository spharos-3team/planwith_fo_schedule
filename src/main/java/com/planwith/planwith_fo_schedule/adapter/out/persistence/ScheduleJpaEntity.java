package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "schedule")
class ScheduleJpaEntity {

	@Id
	private UUID id;

	@Column(name = "owner_id", nullable = false)
	private UUID ownerId;

	@Column(nullable = false, length = 100)
	private String title;

	@OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private final List<ScheduleItemJpaEntity> items = new ArrayList<>();

	protected ScheduleJpaEntity() {
	}

	ScheduleJpaEntity(UUID id, UUID ownerId, String title) {
		this.id = id;
		this.ownerId = ownerId;
		this.title = title;
	}

	void addItem(ScheduleItemJpaEntity item) {
		items.add(item);
		item.assignSchedule(this);
	}

	UUID getId() {
		return id;
	}

	UUID getOwnerId() {
		return ownerId;
	}

	String getTitle() {
		return title;
	}

	List<ScheduleItemJpaEntity> getItems() {
		return List.copyOf(items);
	}
}
