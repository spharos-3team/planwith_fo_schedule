package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.planwith.planwith_fo_schedule.domain.ScheduleType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "schedule_items")
class ScheduleItemJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "schedule_item_id")
	private Long scheduleItemId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "schedule_id", nullable = false)
	private ScheduleJpaEntity schedule;

	@Column(name = "day_number", nullable = false)
	private int dayNumber;

	@Column(name = "schedule_time")
	private LocalTime scheduleTime;

	@Column(nullable = false, length = 200)
	private String subtitle;

	@Enumerated(EnumType.STRING)
	@Column(name = "schedule_type", nullable = false, length = 20)
	private ScheduleType scheduleType;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "estimated_cost", nullable = false)
	private long estimatedCost;

	@Column(name = "place_name", length = 200)
	private String placeName;

	@Column(name = "place_address", length = 500)
	private String placeAddress;

	@Column(precision = 10, scale = 7)
	private BigDecimal latitude;

	@Column(precision = 10, scale = 7)
	private BigDecimal longitude;

	protected ScheduleItemJpaEntity() {
	}

	ScheduleItemJpaEntity(
			Long scheduleItemId,
			int dayNumber,
			LocalTime scheduleTime,
			String subtitle,
			ScheduleType scheduleType,
			String description,
			long estimatedCost,
			String placeName,
			String placeAddress,
			BigDecimal latitude,
			BigDecimal longitude
	) {
		this.scheduleItemId = scheduleItemId;
		this.dayNumber = dayNumber;
		this.scheduleTime = scheduleTime;
		this.subtitle = subtitle;
		this.scheduleType = scheduleType;
		this.description = description;
		this.estimatedCost = estimatedCost;
		this.placeName = placeName;
		this.placeAddress = placeAddress;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	void assignSchedule(ScheduleJpaEntity schedule) { this.schedule = schedule; }
	Long getScheduleItemId() { return scheduleItemId; }
	int getDayNumber() { return dayNumber; }
	LocalTime getScheduleTime() { return scheduleTime; }
	String getSubtitle() { return subtitle; }
	ScheduleType getScheduleType() { return scheduleType; }
	String getDescription() { return description; }
	long getEstimatedCost() { return estimatedCost; }
	String getPlaceName() { return placeName; }
	String getPlaceAddress() { return placeAddress; }
	BigDecimal getLatitude() { return latitude; }
	BigDecimal getLongitude() { return longitude; }
}
