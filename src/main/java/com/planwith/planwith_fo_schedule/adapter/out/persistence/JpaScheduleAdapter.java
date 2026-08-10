package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import java.math.BigDecimal;

import org.springframework.stereotype.Repository;

import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;
import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.GeoPoint;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

@Repository
public class JpaScheduleAdapter implements ScheduleRepositoryPort {

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
				schedule.period().startDate(),
				schedule.period().endDate(),
				schedule.headcount().value(),
				schedule.expectedCost().amount(),
				schedule.transportation(),
				schedule.content(),
				schedule.calendarColor(),
				schedule.creatorType(),
				schedule.createdAt(),
				schedule.updatedAt()
		);
		schedule.items().stream()
				.map(this::toEntity)
				.forEach(entity::addItem);

		return toDomain(scheduleRepository.save(entity));
	}

	private ScheduleItemJpaEntity toEntity(ScheduleItem item) {
		GeoPoint location = item.location();
		return new ScheduleItemJpaEntity(
				item.scheduleItemId(),
				item.dayNumber().value(),
				item.scheduleTime(),
				item.subtitle(),
				item.scheduleType(),
				item.description(),
				item.estimatedCost().amount(),
				item.placeName(),
				item.placeAddress(),
				location == null ? null : location.latitude(),
				location == null ? null : location.longitude()
		);
	}

	private Schedule toDomain(ScheduleJpaEntity entity) {
		return Schedule.restore(
				entity.getScheduleId(),
				new ScheduleUuid(entity.getScheduleUuid()),
				new MemberUuid(entity.getMemberUuid()),
				entity.getTitle(),
				entity.getDestination(),
				new SchedulePeriod(entity.getStartDate(), entity.getEndDate()),
				new Headcount(entity.getHeadcount()),
				new ScheduleCost(entity.getExpectedCost()),
				entity.getTransportation(),
				entity.getContent(),
				entity.getCalendarColor(),
				entity.getCreatorType(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getItems().stream().map(this::toDomain).toList()
		);
	}

	private ScheduleItem toDomain(ScheduleItemJpaEntity entity) {
		return ScheduleItem.restore(
				entity.getScheduleItemId(),
				new DayNumber(entity.getDayNumber()),
				entity.getScheduleTime(),
				entity.getSubtitle(),
				entity.getScheduleType(),
				entity.getDescription(),
				ScheduleCost.of(entity.getEstimatedCost()),
				entity.getPlaceName(),
				entity.getPlaceAddress(),
				toGeoPoint(entity.getLatitude(), entity.getLongitude())
		);
	}

	private GeoPoint toGeoPoint(BigDecimal latitude, BigDecimal longitude) {
		return latitude == null && longitude == null ? null : new GeoPoint(latitude, longitude);
	}
}
