package com.planwith.planwith_fo_schedule.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataScheduleRepository extends JpaRepository<ScheduleJpaEntity, Long> {
}
