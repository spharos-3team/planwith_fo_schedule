package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataScheduleRepository extends JpaRepository<ScheduleJpaEntity, Long> {

	@EntityGraph(attributePaths = "items")
	Optional<ScheduleJpaEntity> findByScheduleUuid(UUID scheduleUuid);
}
