package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.TripType;

public record ScheduleDetailResponse(
		ScheduleResponse schedule,
		FlightResponse flight,
		List<ScheduleItemResponse> items
) {
	public ScheduleDetailResponse {
		items = List.copyOf(items);
	}

	public record ScheduleResponse(
			UUID scheduleUuid,
			String title,
			String destination,
			LocalDate startDate,
			LocalDate endDate,
			int headcount,
			Long expectedCost,
			TransportationType transportation,
			TravelStyle travelStyle,
			String content,
			String calendarColor,
			ScheduleCreatorType creatorType
	) {
	}

	public record ScheduleItemResponse(
			Long scheduleItemId,
			int dayNumber,
			LocalTime scheduleTime,
			String subtitle,
			ScheduleItemType scheduleType,
			String description,
			Long estimatedCost,
			String placeName,
			String placeAddress,
			BigDecimal latitude,
			BigDecimal longitude
	) {
	}

	public record FlightResponse(
			Long scheduleFlightId,
			String provider,
			String departureLocation,
			String originLocationCode,
			String destinationLocation,
			String destinationLocationCode,
			TripType tripType,
			List<FlightSegmentResponse> outbound,
			@JsonProperty("return") List<FlightSegmentResponse> returnSegments
	) {
		public FlightResponse {
			outbound = List.copyOf(outbound);
			returnSegments = List.copyOf(returnSegments);
		}
	}

	public record FlightSegmentResponse(
			Long scheduleFlightSegmentId,
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
	}
}
