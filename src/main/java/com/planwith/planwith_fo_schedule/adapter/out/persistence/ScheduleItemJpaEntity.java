package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;

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
	private int day;

	@Enumerated(EnumType.STRING)
	@Column(name = "schedule_type", nullable = false, length = 20)
	private ScheduleItemType itemType;

	@Column(name = "subtitle", nullable = false, length = 200)
	private String title;

	@Column(name = "description", columnDefinition = "TEXT")
	private String content;

	@Column(name = "place_name", length = 200)
	private String placeName;

	@Column(name = "place_address", length = 500)
	private String placeAddress;

	@Column(precision = 10, scale = 7)
	private BigDecimal latitude;

	@Column(precision = 10, scale = 7)
	private BigDecimal longitude;

	@Column(name = "schedule_time")
	private LocalTime startTime;

	@Column(name = "estimated_cost", nullable = false)
	private long expectedCost;

	protected ScheduleItemJpaEntity() {
	}

	ScheduleItemJpaEntity(
			Long scheduleItemId,
			int day,
			ScheduleItemType itemType,
			String title,
			String content,
			String placeName,
			String placeAddress,
			BigDecimal latitude,
			BigDecimal longitude,
			LocalTime startTime,
			long expectedCost
	) {
		this.scheduleItemId = scheduleItemId;
		this.day = day;
		this.itemType = itemType;
		this.title = title;
		this.content = content;
		this.placeName = placeName;
		this.placeAddress = placeAddress;
		this.latitude = latitude;
		this.longitude = longitude;
		this.startTime = startTime;
		this.expectedCost = expectedCost;
	}

	void assignSchedule(ScheduleJpaEntity schedule) { this.schedule = schedule; }
	Long getScheduleItemId() { return scheduleItemId; }
	Long getScheduleId() { return schedule.getScheduleId(); }
	int getDay() { return day; }
	ScheduleItemType getItemType() { return itemType; }
	String getTitle() { return title; }
	String getContent() { return content; }
	String getPlaceName() { return placeName; }
	String getPlaceAddress() { return placeAddress; }
	BigDecimal getLatitude() { return latitude; }
	BigDecimal getLongitude() { return longitude; }
	LocalTime getStartTime() { return startTime; }
	long getExpectedCost() { return expectedCost; }
}
