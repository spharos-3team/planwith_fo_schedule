package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.DateTimeException;
import java.util.Optional;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.application.port.out.CalendarScheduleQueryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleFlight;
import com.planwith.planwith_fo_schedule.domain.ScheduleFlightSegment;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;
import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.GeoPoint;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleItemLocation;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

@Repository
public class JpaScheduleAdapter implements ScheduleRepositoryPort, CalendarScheduleQueryPort {

	private final SpringDataScheduleRepository scheduleRepository;

	public JpaScheduleAdapter(SpringDataScheduleRepository scheduleRepository) {
		this.scheduleRepository = scheduleRepository;
	}

	@Override
	public Schedule save(Schedule schedule) {
		ScheduleJpaEntity entity = new ScheduleJpaEntity(
				schedule.scheduleId(),
				schedule.scheduleUuid().value(),
				schedule.memberUuid().value(),
				schedule.title(),
				schedule.destination(),
				schedule.imageUrl(),
				schedule.period().startDate(),
				schedule.period().endDate(),
				schedule.headcount().value(),
				schedule.expectedCost().amount(),
				schedule.transportation(),
				schedule.travelStyle(),
				schedule.content(),
				schedule.calendarColor(),
				schedule.creatorType(),
				schedule.createdAt(),
				schedule.updatedAt(),
				schedule.deletedAt()
		);
		schedule.items().stream()
				.map(this::toEntity)
				.forEach(entity::addItem);
		if (schedule.flight() != null) {
			entity.assignFlight(toEntity(schedule.flight()));
		}

		return toDomain(scheduleRepository.save(entity));
	}

	@Override
	public Optional<Schedule> findByScheduleUuid(ScheduleUuid scheduleUuid) {
		return scheduleRepository.findByScheduleUuidAndDeletedAtIsNull(scheduleUuid.value()).map(this::toDomain);
	}

	@Override
	public Schedule update(Schedule schedule) {
		if (schedule.scheduleId() == null) {
			throw new IllegalArgumentException("Persisted schedule ID is required for update.");
		}
		ScheduleJpaEntity entity = scheduleRepository.findById(schedule.scheduleId())
				.orElseThrow(() -> new IllegalStateException("Schedule disappeared during update."));
		entity.updateDetails(
				schedule.title(),
				schedule.destination(),
				schedule.period().startDate(),
				schedule.period().endDate(),
				schedule.headcount().value(),
				schedule.expectedCost().amount(),
				schedule.transportation(),
				schedule.travelStyle(),
				schedule.content(),
				schedule.calendarColor()
		);
		return toDomain(scheduleRepository.save(entity));
	}

	@Override
	public Schedule softDelete(Schedule schedule) {
		if (schedule.scheduleId() == null) {
			throw new IllegalArgumentException("Persisted schedule ID is required for deletion.");
		}
		ScheduleJpaEntity entity = scheduleRepository.findById(schedule.scheduleId())
				.orElseThrow(() -> new IllegalStateException("Schedule disappeared during deletion."));
		entity.markDeleted(schedule.deletedAt());
		return toDomain(scheduleRepository.save(entity));
	}

	@Override
	public List<CalendarScheduleData> findOverlappingSchedules(MemberUuid memberUuid, SchedulePeriod period) {
		return scheduleRepository.findCalendarSchedules(
				memberUuid.value(),
				period.startDate(),
				period.endDate()
		).stream()
				.map(schedule -> new CalendarScheduleData(
						schedule.getScheduleUuid(),
						schedule.getTitle(),
						schedule.getStartDate(),
						schedule.getEndDate(),
						schedule.getCalendarColor(),
						schedule.getCreatorType()
				))
				.toList();
	}

	private ScheduleItemJpaEntity toEntity(ScheduleItem item) {
		ScheduleItemLocation location = item.location();
		GeoPoint coordinates = location == null ? null : location.coordinates();
		return new ScheduleItemJpaEntity(
				item.scheduleItemId(),
				item.day().value(),
				item.itemType(),
				item.title(),
				item.content(),
				location == null ? null : location.placeName(),
				location == null ? null : location.placeAddress(),
				coordinates == null ? null : coordinates.latitude(),
				coordinates == null ? null : coordinates.longitude(),
				item.startTime(),
				item.expectedCost().amount()
		);
	}

	private Schedule toDomain(ScheduleJpaEntity entity) {
		return Schedule.restore(
				entity.getScheduleId(),
				new ScheduleUuid(entity.getScheduleUuid()),
				new MemberUuid(entity.getMemberUuid()),
				entity.getTitle(),
				entity.getDestination(),
				entity.getImageUrl(),
				new SchedulePeriod(entity.getStartDate(), entity.getEndDate()),
				new Headcount(entity.getHeadcount()),
				new ScheduleCost(entity.getExpectedCost()),
				entity.getTransportation(),
				entity.getTravelStyle(),
				entity.getContent(),
				entity.getCalendarColor(),
				entity.getCreatorType(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getDeletedAt(),
				entity.getItems().stream().map(this::toDomain).toList(),
				toDomain(entity.getFlight())
		);
	}

	private ScheduleFlightJpaEntity toEntity(ScheduleFlight flight) {
		ScheduleFlightJpaEntity entity = new ScheduleFlightJpaEntity(
				flight.scheduleFlightId(),
				flight.provider(),
				flight.departureLocation(),
				flight.originLocationCode(),
				flight.destinationLocation(),
				flight.destinationLocationCode(),
				flight.tripType(),
				flight.createdAt(),
				flight.updatedAt()
		);
		flight.segments().stream().map(this::toEntity).forEach(entity::addSegment);
		return entity;
	}

	private ScheduleFlightSegmentJpaEntity toEntity(ScheduleFlightSegment segment) {
		return new ScheduleFlightSegmentJpaEntity(
				segment.scheduleFlightSegmentId(),
				segment.direction(),
				segment.segmentOrder(),
				segment.departureAirportCode(),
				segment.arrivalAirportCode(),
				segment.departureTerminal(),
				segment.arrivalTerminal(),
				segment.departureGate(),
				segment.arrivalGate(),
				toLocalDateTime(segment.departureAt(), segment.departureTimezone()),
				toLocalDateTime(segment.arrivalAt(), segment.arrivalTimezone()),
				segment.departureTimezone(),
				segment.arrivalTimezone(),
				segment.carrierCode(),
				segment.flightNumber(),
				segment.operatingCarrierCode(),
				segment.aircraftCode(),
				segment.flightStatus(),
				segment.durationMinutes(),
				segment.createdAt()
		);
	}

	private ScheduleFlight toDomain(ScheduleFlightJpaEntity entity) {
		if (entity == null) {
			return null;
		}
		return ScheduleFlight.restore(
				entity.getScheduleFlightId(),
				entity.getScheduleId(),
				entity.getProvider(),
				entity.getDepartureLocation(),
				entity.getOriginLocationCode(),
				entity.getDestinationLocation(),
				entity.getDestinationLocationCode(),
				entity.getTripType(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getSegments().stream().map(this::toDomain).toList()
		);
	}

	private ScheduleFlightSegment toDomain(ScheduleFlightSegmentJpaEntity entity) {
		return ScheduleFlightSegment.restore(
				entity.getScheduleFlightSegmentId(),
				entity.getScheduleFlightId(),
				entity.getDirection(),
				entity.getSegmentOrder(),
				entity.getDepartureAirportCode(),
				entity.getArrivalAirportCode(),
				entity.getDepartureTerminal(),
				entity.getArrivalTerminal(),
				entity.getDepartureGate(),
				entity.getArrivalGate(),
				toOffsetDateTime(entity.getDepartureAt(), entity.getDepartureTimezone()),
				toOffsetDateTime(entity.getArrivalAt(), entity.getArrivalTimezone()),
				entity.getDepartureTimezone(),
				entity.getArrivalTimezone(),
				entity.getCarrierCode(),
				entity.getFlightNumber(),
				entity.getOperatingCarrierCode(),
				entity.getAircraftCode(),
				entity.getFlightStatus(),
				entity.getDurationMinutes(),
				entity.getCreatedAt()
		);
	}

	private LocalDateTime toLocalDateTime(OffsetDateTime value, String timezone) {
		try {
			return value.atZoneSameInstant(ZoneId.of(timezone)).toLocalDateTime();
		} catch (DateTimeException exception) {
			return value.toLocalDateTime();
		}
	}

	private OffsetDateTime toOffsetDateTime(LocalDateTime value, String timezone) {
		try {
			return value.atZone(ZoneId.of(timezone)).toOffsetDateTime();
		} catch (DateTimeException exception) {
			return value.atZone(ZoneId.of("UTC")).toOffsetDateTime();
		}
	}

	private ScheduleItem toDomain(ScheduleItemJpaEntity entity) {
		return ScheduleItem.restore(
				entity.getScheduleItemId(),
				entity.getScheduleId(),
				new DayNumber(entity.getDay()),
				entity.getItemType(),
				entity.getTitle(),
				entity.getContent(),
				toLocation(entity),
				entity.getStartTime(),
				ScheduleCost.of(entity.getExpectedCost())
		);
	}

	private ScheduleItemLocation toLocation(ScheduleItemJpaEntity entity) {
		GeoPoint coordinates = toGeoPoint(entity.getLatitude(), entity.getLongitude());
		if (entity.getPlaceName() == null && entity.getPlaceAddress() == null && coordinates == null) {
			return null;
		}
		return new ScheduleItemLocation(entity.getPlaceName(), entity.getPlaceAddress(), coordinates);
	}

	private GeoPoint toGeoPoint(BigDecimal latitude, BigDecimal longitude) {
		return latitude == null && longitude == null ? null : new GeoPoint(latitude, longitude);
	}
}
