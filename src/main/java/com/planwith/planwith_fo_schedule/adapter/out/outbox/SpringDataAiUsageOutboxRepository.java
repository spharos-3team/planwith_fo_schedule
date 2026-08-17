package com.planwith.planwith_fo_schedule.adapter.out.outbox;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

interface SpringDataAiUsageOutboxRepository extends JpaRepository<AiUsageOutboxJpaEntity, Long> {

	boolean existsByRequestId(UUID requestId);

	Optional<AiUsageOutboxJpaEntity> findByRequestId(UUID requestId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select outbox
			from AiUsageOutboxJpaEntity outbox
			where outbox.status = :status
			  and outbox.nextAttemptAt <= :now
			order by outbox.createdAt asc
			""")
	List<AiUsageOutboxJpaEntity> findReadyToPublish(
			@Param("status") AiUsageOutboxStatus status,
			@Param("now") Instant now,
			Pageable pageable
	);
}
