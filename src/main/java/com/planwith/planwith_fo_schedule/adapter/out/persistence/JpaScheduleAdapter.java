package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import org.springframework.stereotype.Repository;

import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.ScheduleItem;

@Repository
public class JpaScheduleAdapter implements ScheduleRepositoryPort {

	private final SpringDataScheduleRepository scheduleRepository;

	public JpaScheduleAdapter(SpringDataScheduleRepository scheduleRepository) {
		this.scheduleRepository = scheduleRepository;
	}

	@Override
	public Schedule save(Schedule schedule) {
		ScheduleJpaEntity entity = new ScheduleJpaEntity(
				schedule.id(),
				schedule.ownerId(),
				schedule.title()
		);
		schedule.items().stream()
				.map(this::toEntity)
				.forEach(entity::addItem);

		return toDomain(scheduleRepository.save(entity));
	}

	private ScheduleItemJpaEntity toEntity(ScheduleItem item) {
		return new ScheduleItemJpaEntity(item.id(), item.title(), item.startsAt(), item.endsAt());
	}

	private Schedule toDomain(ScheduleJpaEntity entity) {
		return Schedule.restore(
				entity.getId(),
				entity.getOwnerId(),
				entity.getTitle(),
				entity.getItems().stream()
						.map(item -> new ScheduleItem(
								item.getId(),
								item.getTitle(),
								item.getStartsAt(),
								item.getEndsAt()
						))
						.toList()
		);
	}
}
