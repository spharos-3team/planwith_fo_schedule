package com.planwith.planwith_fo_schedule.adapter.out.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_schedule.application.model.AiUsageReportEvent;
import com.planwith.planwith_fo_schedule.application.port.out.AiUsageReportingPort;

@Component
@ConditionalOnProperty(
		name = "ai.usage-report.enabled",
		havingValue = "false",
		matchIfMissing = true
)
public class DisabledAiUsageReportingAdapter implements AiUsageReportingPort {

	private static final Logger log = LoggerFactory.getLogger(DisabledAiUsageReportingAdapter.class);

	@Override
	public void report(AiUsageReportEvent event) {
		log.debug("DisabledAiUsageReportingAdapter : report : AI 사용량 이벤트 발행 비활성화 - requestId={}",
				event.requestId());
	}
}
