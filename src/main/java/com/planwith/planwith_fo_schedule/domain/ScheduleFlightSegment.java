package com.planwith.planwith_fo_schedule.domain;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.DateTimeException;
import java.util.Locale;
import java.util.Objects;

public final class ScheduleFlightSegment {

	private final Long scheduleFlightSegmentId;
	private final Long scheduleFlightId;
	private final FlightDirection direction;
	private final int segmentOrder;
	private final String departureAirportCode;
	private final String arrivalAirportCode;
	private final String departureTerminal;
	private final String arrivalTerminal;
	private final String departureGate;
	private final String arrivalGate;
	private final OffsetDateTime departureAt;
	private final OffsetDateTime arrivalAt;
	private final String departureTimezone;
	private final String arrivalTimezone;
	private final String carrierCode;
	private final String flightNumber;
	private final String operatingCarrierCode;
	private final String aircraftCode;
	private final String flightStatus;
	private final Integer durationMinutes;
	private final LocalDateTime createdAt;

	private ScheduleFlightSegment(
			Long scheduleFlightSegmentId,
			Long scheduleFlightId,
			FlightDirection direction,
			int segmentOrder,
			String departureAirportCode,
			String arrivalAirportCode,
			String departureTerminal,
			String arrivalTerminal,
			String departureGate,
			String arrivalGate,
			OffsetDateTime departureAt,
			OffsetDateTime arrivalAt,
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
		if (segmentOrder < 1) {
			throw new InvalidScheduleException("Flight segment order must be at least 1.");
		}
		this.scheduleFlightSegmentId = scheduleFlightSegmentId;
		this.scheduleFlightId = scheduleFlightId;
		this.direction = Objects.requireNonNull(direction, "Flight direction is required.");
		this.segmentOrder = segmentOrder;
		this.departureAirportCode = requireIata(departureAirportCode, "Departure airport code is required.");
		this.arrivalAirportCode = requireIata(arrivalAirportCode, "Arrival airport code is required.");
		if (this.departureAirportCode.equals(this.arrivalAirportCode)) {
			throw new InvalidScheduleException("Departure and arrival airports must be different.");
		}
		this.departureTerminal = optionalText(departureTerminal, 20, "Departure terminal");
		this.arrivalTerminal = optionalText(arrivalTerminal, 20, "Arrival terminal");
		this.departureGate = optionalText(departureGate, 20, "Departure gate");
		this.arrivalGate = optionalText(arrivalGate, 20, "Arrival gate");
		this.departureAt = Objects.requireNonNull(departureAt, "Flight departure time is required.");
		this.arrivalAt = Objects.requireNonNull(arrivalAt, "Flight arrival time is required.");
		if (!this.arrivalAt.isAfter(this.departureAt)) {
			throw new InvalidScheduleException("Flight arrival time must be after departure time.");
		}
		this.departureTimezone = requireTimezone(departureTimezone, "Departure timezone is required.");
		this.arrivalTimezone = requireTimezone(arrivalTimezone, "Arrival timezone is required.");
		this.carrierCode = requireCarrierCode(carrierCode, "Carrier code is required.");
		this.flightNumber = requireText(flightNumber, 10, "Flight number is required.");
		this.operatingCarrierCode = optionalCarrierCode(operatingCarrierCode);
		this.aircraftCode = optionalText(aircraftCode, 10, "Aircraft code");
		this.flightStatus = optionalText(flightStatus, 30, "Flight status");
		if (durationMinutes != null && durationMinutes < 0) {
			throw new InvalidScheduleException("Flight duration must not be negative.");
		}
		this.durationMinutes = durationMinutes;
		this.createdAt = createdAt;
	}

	public static ScheduleFlightSegment create(
			FlightDirection direction,
			int segmentOrder,
			String departureAirportCode,
			String arrivalAirportCode,
			String departureTerminal,
			String arrivalTerminal,
			String departureGate,
			String arrivalGate,
			OffsetDateTime departureAt,
			OffsetDateTime arrivalAt,
			String departureTimezone,
			String arrivalTimezone,
			String carrierCode,
			String flightNumber,
			String operatingCarrierCode,
			String aircraftCode,
			String flightStatus,
			Integer durationMinutes
	) {
		return new ScheduleFlightSegment(
				null, null, direction, segmentOrder, departureAirportCode, arrivalAirportCode,
				departureTerminal, arrivalTerminal, departureGate, arrivalGate, departureAt, arrivalAt,
				departureTimezone, arrivalTimezone, carrierCode, flightNumber, operatingCarrierCode,
				aircraftCode, flightStatus, durationMinutes, null
		);
	}

	public static ScheduleFlightSegment restore(
			Long scheduleFlightSegmentId,
			Long scheduleFlightId,
			FlightDirection direction,
			int segmentOrder,
			String departureAirportCode,
			String arrivalAirportCode,
			String departureTerminal,
			String arrivalTerminal,
			String departureGate,
			String arrivalGate,
			OffsetDateTime departureAt,
			OffsetDateTime arrivalAt,
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
		return new ScheduleFlightSegment(
				scheduleFlightSegmentId, scheduleFlightId, direction, segmentOrder,
				departureAirportCode, arrivalAirportCode, departureTerminal, arrivalTerminal,
				departureGate, arrivalGate, departureAt, arrivalAt, departureTimezone, arrivalTimezone,
				carrierCode, flightNumber, operatingCarrierCode, aircraftCode, flightStatus,
				durationMinutes, createdAt
		);
	}

	private static String requireIata(String value, String message) {
		String code = requireText(value, 3, message).toUpperCase(Locale.ROOT);
		if (!code.matches("[A-Z]{3}")) {
			throw new InvalidScheduleException("Airport IATA code must contain three letters.");
		}
		return code;
	}

	private static String requireCarrierCode(String value, String message) {
		String code = requireText(value, 3, message).toUpperCase(Locale.ROOT);
		if (!code.matches("[A-Z0-9]{2,3}")) {
			throw new InvalidScheduleException("Carrier code must contain two or three letters or digits.");
		}
		return code;
	}

	private static String optionalCarrierCode(String value) {
		String code = trimToNull(value);
		return code == null ? null : requireCarrierCode(code, "Operating carrier code is required.");
	}

	private static String requireTimezone(String value, String message) {
		String timezone = requireText(value, 50, message);
		try {
			ZoneId.of(timezone);
			return timezone;
		} catch (DateTimeException exception) {
			throw new InvalidScheduleException("Flight timezone is invalid.");
		}
	}

	private static String requireText(String value, int maxLength, String message) {
		String text = trimToNull(value);
		if (text == null) {
			throw new InvalidScheduleException(message);
		}
		if (text.length() > maxLength) {
			throw new InvalidScheduleException(message + " Maximum length is " + maxLength + ".");
		}
		return text;
	}

	private static String optionalText(String value, int maxLength, String fieldName) {
		String text = trimToNull(value);
		if (text != null && text.length() > maxLength) {
			throw new InvalidScheduleException(fieldName + " must not exceed " + maxLength + " characters.");
		}
		return text;
	}

	private static String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	public Long scheduleFlightSegmentId() { return scheduleFlightSegmentId; }
	public Long scheduleFlightId() { return scheduleFlightId; }
	public FlightDirection direction() { return direction; }
	public int segmentOrder() { return segmentOrder; }
	public String departureAirportCode() { return departureAirportCode; }
	public String arrivalAirportCode() { return arrivalAirportCode; }
	public String departureTerminal() { return departureTerminal; }
	public String arrivalTerminal() { return arrivalTerminal; }
	public String departureGate() { return departureGate; }
	public String arrivalGate() { return arrivalGate; }
	public OffsetDateTime departureAt() { return departureAt; }
	public OffsetDateTime arrivalAt() { return arrivalAt; }
	public String departureTimezone() { return departureTimezone; }
	public String arrivalTimezone() { return arrivalTimezone; }
	public String carrierCode() { return carrierCode; }
	public String flightNumber() { return flightNumber; }
	public String operatingCarrierCode() { return operatingCarrierCode; }
	public String aircraftCode() { return aircraftCode; }
	public String flightStatus() { return flightStatus; }
	public Integer durationMinutes() { return durationMinutes; }
	public LocalDateTime createdAt() { return createdAt; }
}
