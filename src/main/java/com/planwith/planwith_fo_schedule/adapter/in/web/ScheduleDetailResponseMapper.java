package com.planwith.planwith_fo_schedule.adapter.in.web;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ScheduleDetailResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ScheduleDetailResponse.FlightResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ScheduleDetailResponse.FlightSegmentResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ScheduleDetailResponse.ScheduleItemResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ScheduleDetailResponse.ScheduleResponse;
import com.planwith.planwith_fo_schedule.application.port.in.GetScheduleDetailUseCase.FlightResult;
import com.planwith.planwith_fo_schedule.application.port.in.GetScheduleDetailUseCase.FlightSegmentResult;
import com.planwith.planwith_fo_schedule.application.port.in.GetScheduleDetailUseCase.ScheduleDetailResult;

final class ScheduleDetailResponseMapper {

	private ScheduleDetailResponseMapper() {
	}

	static ScheduleDetailResponse toResponse(ScheduleDetailResult result) {
		return new ScheduleDetailResponse(
				new ScheduleResponse(
						result.schedule().scheduleUuid(), result.schedule().title(), result.schedule().destination(),
						result.schedule().startDate(), result.schedule().endDate(), result.schedule().headcount(),
						result.schedule().expectedCost(), result.schedule().transportation(),
						result.schedule().travelStyle(), result.schedule().content(),
						result.schedule().calendarColor(), result.schedule().creatorType()
				),
				toFlightResponse(result.flight()),
				result.items().stream().map(item -> new ScheduleItemResponse(
						item.scheduleItemId(), item.dayNumber(), item.scheduleTime(), item.subtitle(),
						item.scheduleType(), item.description(), item.estimatedCost(), item.placeName(),
						item.placeAddress(), item.latitude(), item.longitude()
				)).toList()
		);
	}

	private static FlightResponse toFlightResponse(FlightResult flight) {
		if (flight == null) {
			return null;
		}
		return new FlightResponse(
				flight.scheduleFlightId(), flight.provider(), flight.departureLocation(),
				flight.originLocationCode(), flight.destinationLocation(), flight.destinationLocationCode(),
				flight.tripType(),
				flight.outbound().stream().map(ScheduleDetailResponseMapper::toSegmentResponse).toList(),
				flight.returnSegments().stream().map(ScheduleDetailResponseMapper::toSegmentResponse).toList()
		);
	}

	private static FlightSegmentResponse toSegmentResponse(FlightSegmentResult segment) {
		return new FlightSegmentResponse(
				segment.scheduleFlightSegmentId(), segment.segmentOrder(),
				segment.departureAirportCode(), segment.arrivalAirportCode(),
				segment.departureTerminal(), segment.arrivalTerminal(),
				segment.departureGate(), segment.arrivalGate(), segment.departureAt(), segment.arrivalAt(),
				segment.departureTimezone(), segment.arrivalTimezone(), segment.carrierCode(),
				segment.flightNumber(), segment.operatingCarrierCode(), segment.aircraftCode(),
				segment.flightStatus(), segment.durationMinutes()
		);
	}
}
