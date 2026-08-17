package com.planwith.planwith_fo_schedule.adapter.out.outbox;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_schedule.application.model.AiUsageReportEvent;
import com.planwith.planwith_fo_schedule.application.port.out.AiUsageReportingPort;

@Component
public class OutboxAiUsageReportingAdapter implements AiUsageReportingPort {

	private static final Logger log = LoggerFactory.getLogger(OutboxAiUsageReportingAdapter.class);

	private final SpringDataAiUsageOutboxRepository repository;

	public OutboxAiUsageReportingAdapter(SpringDataAiUsageOutboxRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public void report(AiUsageReportEvent event) {
		AiUsageReportEvent validatedEvent = Objects.requireNonNull(event, "AI usage report event is required.");
		if (repository.existsByRequestId(validatedEvent.requestId())) {
			log.warn("OutboxAiUsageReportingAdapter : report : 중복 AI 사용량 이벤트 저장 생략 - requestId={}",
					validatedEvent.requestId());
			return;
		}

		try {
			repository.saveAndFlush(AiUsageOutboxJpaEntity.from(validatedEvent));
			log.info("OutboxAiUsageReportingAdapter : report : AI 사용량 Outbox 저장 완료 - requestId={}, "
							+ "memberUuid={}, operationType={}",
					validatedEvent.requestId(), validatedEvent.memberUuid(), validatedEvent.operationType());
		} catch (DataIntegrityViolationException exception) {
			if (!repository.existsByRequestId(validatedEvent.requestId())) {
				throw exception;
			}
			log.warn("OutboxAiUsageReportingAdapter : report : 동시 중복 AI 사용량 이벤트 저장 생략 - requestId={}",
					validatedEvent.requestId());
		}
	}
}
