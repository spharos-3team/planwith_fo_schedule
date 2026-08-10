package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataScheduleRepository extends JpaRepository<ScheduleJpaEntity, UUID> {
}
