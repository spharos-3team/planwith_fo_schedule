package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.planwith.planwith_fo_schedule.domain.FlightDirection;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "schedule_flight_segment",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_schedule_flight_segment_order",
				columnNames = {"schedule_flight_id", "direction", "segment_order"}
		),
		indexes = @Index(
				name = "idx_schedule_flight_segment",
				columnList = "schedule_flight_id,direction,segment_order"
		)
)
class ScheduleFlightSegmentJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "schedule_flight_segment_id")
	private Long scheduleFlightSegmentId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "schedule_flight_id", nullable = false)
	private ScheduleFlightJpaEntity scheduleFlight;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private FlightDirection direction;

	@Column(name = "segment_order", nullable = false)
	private int segmentOrder;

	@Column(name = "departure_airport_code", nullable = false, length = 3)
	private String departureAirportCode;

	@Column(name = "arrival_airport_code", nullable = false, length = 3)
	private String arrivalAirportCode;

	@Column(name = "departure_terminal", length = 20)
	private String departureTerminal;

	@Column(name = "arrival_terminal", length = 20)
	private String arrivalTerminal;

	@Column(name = "departure_gate", length = 20)
	private String departureGate;

	@Column(name = "arrival_gate", length = 20)
	private String arrivalGate;

	@Column(name = "departure_at", nullable = false)
	private LocalDateTime departureAt;

	@Column(name = "arrival_at", nullable = false)
	private LocalDateTime arrivalAt;

	@Column(name = "departure_timezone", nullable = false, length = 50)
	private String departureTimezone;

	@Column(name = "arrival_timezone", nullable = false, length = 50)
	private String arrivalTimezone;

	@Column(name = "carrier_code", nullable = false, length = 3)
	private String carrierCode;

	@Column(name = "flight_number", nullable = false, length = 10)
	private String flightNumber;

	@Column(name = "operating_carrier_code", length = 3)
	private String operatingCarrierCode;

	@Column(name = "aircraft_code", length = 10)
	private String aircraftCode;

	@Column(name = "flight_status", length = 30)
	private String flightStatus;

	@Column(name = "duration_minutes")
	private Integer durationMinutes;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected ScheduleFlightSegmentJpaEntity() {
	}

	ScheduleFlightSegmentJpaEntity(
			Long scheduleFlightSegmentId,
			FlightDirection direction,
			int segmentOrder,
			String departureAirportCode,
			String arrivalAirportCode,
			String departureTerminal,
			String arrivalTerminal,
			String departureGate,
			String arrivalGate,
			LocalDateTime departureAt,
			LocalDateTime arrivalAt,
			String departureTimezone,
			String arrivalTimezone,
			String carrierCode,
			String flightNumber,
			String operatingCarrierCode,
			String aircraftCode,
			String flightStatus,
			Integer durationMinutes,
			LocalDateTime createdAt
	) {
		this.scheduleFlightSegmentId = scheduleFlightSegmentId;
		this.direction = direction;
		this.segmentOrder = segmentOrder;
		this.departureAirportCode = departureAirportCode;
		this.arrivalAirportCode = arrivalAirportCode;
		this.departureTerminal = departureTerminal;
		this.arrivalTerminal = arrivalTerminal;
		this.departureGate = departureGate;
		this.arrivalGate = arrivalGate;
		this.departureAt = departureAt;
		this.arrivalAt = arrivalAt;
		this.departureTimezone = departureTimezone;
		this.arrivalTimezone = arrivalTimezone;
		this.carrierCode = carrierCode;
		this.flightNumber = flightNumber;
		this.operatingCarrierCode = operatingCarrierCode;
		this.aircraftCode = aircraftCode;
		this.flightStatus = flightStatus;
		this.durationMinutes = durationMinutes;
		this.createdAt = createdAt;
	}

	void assignScheduleFlight(ScheduleFlightJpaEntity scheduleFlight) {
		this.scheduleFlight = scheduleFlight;
	}

	Long getScheduleFlightSegmentId() { return scheduleFlightSegmentId; }
	Long getScheduleFlightId() { return scheduleFlight.getScheduleFlightId(); }
	FlightDirection getDirection() { return direction; }
	int getSegmentOrder() { return segmentOrder; }
	String getDepartureAirportCode() { return departureAirportCode; }
	String getArrivalAirportCode() { return arrivalAirportCode; }
	String getDepartureTerminal() { return departureTerminal; }
	String getArrivalTerminal() { return arrivalTerminal; }
	String getDepartureGate() { return departureGate; }
	String getArrivalGate() { return arrivalGate; }
	LocalDateTime getDepartureAt() { return departureAt; }
	LocalDateTime getArrivalAt() { return arrivalAt; }
	String getDepartureTimezone() { return departureTimezone; }
	String getArrivalTimezone() { return arrivalTimezone; }
	String getCarrierCode() { return carrierCode; }
	String getFlightNumber() { return flightNumber; }
	String getOperatingCarrierCode() { return operatingCarrierCode; }
	String getAircraftCode() { return aircraftCode; }
	String getFlightStatus() { return flightStatus; }
	Integer getDurationMinutes() { return durationMinutes; }
	LocalDateTime getCreatedAt() { return createdAt; }
}
