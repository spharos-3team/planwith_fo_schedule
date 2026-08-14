package com.planwith.planwith_fo_schedule.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.TripType;

public interface GetScheduleDetailUseCase {

	ScheduleDetailResult getScheduleDetail(UUID scheduleUuid);

	record ScheduleDetailResult(
			ScheduleResult schedule,
			FlightResult flight,
			List<ScheduleItemResult> items
	) {
		public ScheduleDetailResult {
			items = List.copyOf(items);
		}
	}

	record ScheduleResult(
			UUID scheduleUuid,
			String title,
			String destination,
			String imageUrl,
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

	record ScheduleItemResult(
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

	record FlightResult(
			Long scheduleFlightId,
			String provider,
			String departureLocation,
			String originLocationCode,
			String destinationLocation,
			String destinationLocationCode,
			TripType tripType,
			List<FlightSegmentResult> outbound,
			List<FlightSegmentResult> returnSegments
	) {
		public FlightResult {
			outbound = List.copyOf(outbound);
			returnSegments = List.copyOf(returnSegments);
		}
	}

	record FlightSegmentResult(
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
