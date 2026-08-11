package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataScheduleRepository extends JpaRepository<ScheduleJpaEntity, Long> {

	@EntityGraph(attributePaths = "items")
	Optional<ScheduleJpaEntity> findByScheduleUuidAndDeletedAtIsNull(UUID scheduleUuid);

	@Query("""
			select
				s.scheduleUuid as scheduleUuid,
				s.title as title,
				s.startDate as startDate,
				s.endDate as endDate,
				s.calendarColor as calendarColor,
				s.creatorType as creatorType
			from ScheduleJpaEntity s
			where s.deletedAt is null
			  and s.startDate <= :endDate
			  and s.endDate >= :startDate
			order by s.startDate asc, s.endDate asc, s.scheduleId asc
			""")
	List<CalendarScheduleProjection> findCalendarSchedules(
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate
	);
}
