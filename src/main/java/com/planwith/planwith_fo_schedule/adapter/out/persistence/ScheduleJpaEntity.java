package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_schedule.domain.CreatorType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "schedule")
class ScheduleJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "schedule_id")
	private Long scheduleId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "schedule_uuid", nullable = false, unique = true, length = 36)
	private UUID scheduleUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, length = 200)
	private String destination;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Column(nullable = false)
	private int headcount;

	@Column(name = "expected_cost")
	private Long expectedCost;

	@Column(columnDefinition = "TEXT")
	private String transportation;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(name = "calendar_color", length = 30)
	private String calendarColor;

	@Enumerated(EnumType.STRING)
	@Column(name = "creator_type", nullable = false, length = 10)
	private CreatorType creatorType;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private final List<ScheduleItemJpaEntity> items = new ArrayList<>();

	@OneToOne(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private ScheduleFlightJpaEntity flight;

	protected ScheduleJpaEntity() {
	}

	ScheduleJpaEntity(
			Long scheduleId,
			UUID scheduleUuid,
			UUID memberUuid,
			String title,
			String destination,
			LocalDate startDate,
			LocalDate endDate,
			int headcount,
			Long expectedCost,
			String transportation,
			String content,
			String calendarColor,
			CreatorType creatorType,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			LocalDateTime deletedAt
	) {
		this.scheduleId = scheduleId;
		this.scheduleUuid = scheduleUuid;
		this.memberUuid = memberUuid;
		this.title = title;
		this.destination = destination;
		this.startDate = startDate;
		this.endDate = endDate;
		this.headcount = headcount;
		this.expectedCost = expectedCost;
		this.transportation = transportation;
		this.content = content;
		this.calendarColor = calendarColor;
		this.creatorType = creatorType;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}

	void addItem(ScheduleItemJpaEntity item) {
		items.add(item);
		item.assignSchedule(this);
	}

	void assignFlight(ScheduleFlightJpaEntity flight) {
		this.flight = flight;
		flight.assignSchedule(this);
	}

	void updateDetails(
			String title,
			String destination,
			LocalDate startDate,
			LocalDate endDate,
			int headcount,
			Long expectedCost,
			String transportation,
			String content,
			String calendarColor
	) {
		this.title = title;
		this.destination = destination;
		this.startDate = startDate;
		this.endDate = endDate;
		this.headcount = headcount;
		this.expectedCost = expectedCost;
		this.transportation = transportation;
		this.content = content;
		this.calendarColor = calendarColor;
	}

	void markDeleted(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	Long getScheduleId() { return scheduleId; }
	UUID getScheduleUuid() { return scheduleUuid; }
	UUID getMemberUuid() { return memberUuid; }
	String getTitle() { return title; }
	String getDestination() { return destination; }
	LocalDate getStartDate() { return startDate; }
	LocalDate getEndDate() { return endDate; }
	int getHeadcount() { return headcount; }
	Long getExpectedCost() { return expectedCost; }
	String getTransportation() { return transportation; }
	String getContent() { return content; }
	String getCalendarColor() { return calendarColor; }
	CreatorType getCreatorType() { return creatorType; }
	LocalDateTime getCreatedAt() { return createdAt; }
	LocalDateTime getUpdatedAt() { return updatedAt; }
	LocalDateTime getDeletedAt() { return deletedAt; }
	List<ScheduleItemJpaEntity> getItems() { return List.copyOf(items); }
	ScheduleFlightJpaEntity getFlight() { return flight; }
}
