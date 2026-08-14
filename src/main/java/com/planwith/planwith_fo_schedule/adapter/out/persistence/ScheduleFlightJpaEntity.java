package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.planwith.planwith_fo_schedule.domain.TripType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(
		name = "schedule_flight",
		indexes = @Index(
				name = "idx_schedule_flight_route",
				columnList = "origin_location_code,destination_location_code"
		)
)
class ScheduleFlightJpaEntity {
	private static final String DEFAULT_PROVIDER = "AVIATIONSTACK";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "schedule_flight_id")
	private Long scheduleFlightId;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "schedule_id", nullable = false, unique = true)
	private ScheduleJpaEntity schedule;

	@Column(nullable = false, length = 30)
	private String provider;

	@Column(name = "departure_location", nullable = false, length = 200)
	private String departureLocation;

	@Column(name = "origin_location_code", nullable = false, length = 3)
	private String originLocationCode;

	@Column(name = "destination_location", nullable = false, length = 200)
	private String destinationLocation;

	@Column(name = "destination_location_code", nullable = false, length = 3)
	private String destinationLocationCode;

	@Enumerated(EnumType.STRING)
	@Column(name = "trip_type", nullable = false, length = 20)
	private TripType tripType;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "scheduleFlight", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("direction ASC, segmentOrder ASC")
	private final List<ScheduleFlightSegmentJpaEntity> segments = new ArrayList<>();

	protected ScheduleFlightJpaEntity() {
	}

	ScheduleFlightJpaEntity(
			Long scheduleFlightId,
			String provider,
			String departureLocation,
			String originLocationCode,
			String destinationLocation,
			String destinationLocationCode,
			TripType tripType,
			LocalDateTime createdAt,
			LocalDateTime updatedAt
	) {
		this.scheduleFlightId = scheduleFlightId;
		this.provider = provider == null || provider.isBlank() ? DEFAULT_PROVIDER : provider.trim();
		this.departureLocation = departureLocation;
		this.originLocationCode = originLocationCode;
		this.destinationLocation = destinationLocation;
		this.destinationLocationCode = destinationLocationCode;
		this.tripType = tripType == null ? TripType.ROUND_TRIP : tripType;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	void assignSchedule(ScheduleJpaEntity schedule) { this.schedule = schedule; }

	void addSegment(ScheduleFlightSegmentJpaEntity segment) {
		segments.add(segment);
		segment.assignScheduleFlight(this);
	}

	Long getScheduleFlightId() { return scheduleFlightId; }
	Long getScheduleId() { return schedule.getScheduleId(); }
	String getProvider() { return provider; }
	String getDepartureLocation() { return departureLocation; }
	String getOriginLocationCode() { return originLocationCode; }
	String getDestinationLocation() { return destinationLocation; }
	String getDestinationLocationCode() { return destinationLocationCode; }
	TripType getTripType() { return tripType; }
	LocalDateTime getCreatedAt() { return createdAt; }
	LocalDateTime getUpdatedAt() { return updatedAt; }
	List<ScheduleFlightSegmentJpaEntity> getSegments() { return List.copyOf(segments); }
}
