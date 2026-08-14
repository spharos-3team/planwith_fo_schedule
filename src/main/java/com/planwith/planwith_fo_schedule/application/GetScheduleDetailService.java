package com.planwith.planwith_fo_schedule.application;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.exception.ScheduleNotFoundException;
import com.planwith.planwith_fo_schedule.application.port.in.GetScheduleDetailUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.FlightDirection;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleFlight;
import com.planwith.planwith_fo_schedule.domain.ScheduleFlightSegment;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;
import com.planwith.planwith_fo_schedule.domain.vo.GeoPoint;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleItemLocation;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

@Service
public class GetScheduleDetailService implements GetScheduleDetailUseCase {

	private static final Logger log = LoggerFactory.getLogger(GetScheduleDetailService.class);

	private final ScheduleRepositoryPort scheduleRepositoryPort;

	public GetScheduleDetailService(ScheduleRepositoryPort scheduleRepositoryPort) {
		this.scheduleRepositoryPort = scheduleRepositoryPort;
	}

	@Override
	@Transactional(readOnly = true)
	public ScheduleDetailResult getScheduleDetail(UUID scheduleUuid) {
		UUID validatedScheduleUuid = Objects.requireNonNull(scheduleUuid, "Schedule UUID is required.");
		log.info("GetScheduleDetailService : getScheduleDetail : 일정 상세 통합 조회 시작 - scheduleUuid={}",
				validatedScheduleUuid);
		Schedule schedule = scheduleRepositoryPort.findByScheduleUuid(new ScheduleUuid(validatedScheduleUuid))
				.orElseThrow(() -> new ScheduleNotFoundException(validatedScheduleUuid));

		ScheduleDetailResult result = new ScheduleDetailResult(
				toScheduleResult(schedule),
				toFlightResult(schedule.flight()),
				schedule.items().stream()
						.sorted(Comparator.comparingInt((ScheduleItem item) -> item.day().value())
								.thenComparing(item -> item.startTime(), Comparator.nullsLast(Comparator.naturalOrder()))
								.thenComparing(item -> item.scheduleItemId(),
										Comparator.nullsLast(Comparator.naturalOrder())))
						.map(this::toItemResult)
						.toList()
		);
		log.info("GetScheduleDetailService : getScheduleDetail : 일정 상세 통합 조회 완료 - scheduleUuid={}, "
				+ "itemCount={}, flightIncluded={}",
				validatedScheduleUuid, result.items().size(), result.flight() != null);
		return result;
	}

	private ScheduleResult toScheduleResult(Schedule schedule) {
		return new ScheduleResult(
				schedule.scheduleUuid().value(), schedule.title(), schedule.destination(),
				schedule.imageUrl(),
				schedule.period().startDate(), schedule.period().endDate(), schedule.headcount().value(),
				schedule.expectedCost().amount(), schedule.transportation(), schedule.travelStyle(),
				schedule.content(), schedule.calendarColor(), schedule.creatorType()
		);
	}

	private ScheduleItemResult toItemResult(ScheduleItem item) {
		ScheduleItemLocation location = item.location();
		GeoPoint coordinates = location == null ? null : location.coordinates();
		return new ScheduleItemResult(
				item.scheduleItemId(), item.day().value(), item.startTime(), item.title(), item.itemType(),
				item.content(), item.expectedCost().amount(),
				location == null ? null : location.placeName(),
				location == null ? null : location.placeAddress(),
				coordinates == null ? null : coordinates.latitude(),
				coordinates == null ? null : coordinates.longitude()
		);
	}

	private FlightResult toFlightResult(ScheduleFlight flight) {
		if (flight == null) {
			return null;
		}
		return new FlightResult(
				flight.scheduleFlightId(), flight.provider(), flight.departureLocation(),
				flight.originLocationCode(), flight.destinationLocation(), flight.destinationLocationCode(),
				flight.tripType(), segments(flight, FlightDirection.OUTBOUND), segments(flight, FlightDirection.RETURN)
		);
	}

	private List<FlightSegmentResult> segments(ScheduleFlight flight, FlightDirection direction) {
		return flight.segments().stream()
				.filter(segment -> segment.direction() == direction)
				.map(this::toSegmentResult)
				.toList();
	}

	private FlightSegmentResult toSegmentResult(ScheduleFlightSegment segment) {
		return new FlightSegmentResult(
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
