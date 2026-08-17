package com.planwith.planwith_fo_schedule.adapter.out.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_schedule.application.model.AiUsageReportEvent;
import com.planwith.planwith_fo_schedule.application.port.out.AiUsageReportingPort;
import com.planwith.planwith_fo_schedule.config.AiUsageReportProperties;

@Component
@ConditionalOnProperty(name = "ai.usage-report.enabled", havingValue = "true")
public class KafkaAiUsageReportingAdapter implements AiUsageReportingPort {

	private static final Logger log = LoggerFactory.getLogger(KafkaAiUsageReportingAdapter.class);

	private final KafkaTemplate<String, AiUsageReportEvent> kafkaTemplate;
	private final String topic;

	public KafkaAiUsageReportingAdapter(
			KafkaTemplate<String, AiUsageReportEvent> kafkaTemplate,
			AiUsageReportProperties properties
	) {
		this.kafkaTemplate = kafkaTemplate;
		if (properties.getTopic() == null || properties.getTopic().isBlank()) {
			throw new IllegalArgumentException("AI usage report Kafka topic is required.");
		}
		this.topic = properties.getTopic();
	}

	@Override
	public void report(AiUsageReportEvent event) {
		String messageKey = event.requestId().toString();
		log.info("KafkaAiUsageReportingAdapter : report : AI 사용량 이벤트 발행 시작 - requestId={}, "
						+ "memberUuid={}, operationType={}, topic={}",
				event.requestId(), event.memberUuid(), event.operationType(), topic);
		kafkaTemplate.send(topic, messageKey, event)
				.whenComplete((result, exception) -> {
					if (exception != null) {
						log.error("KafkaAiUsageReportingAdapter : report : AI 사용량 이벤트 발행 실패 - "
										+ "requestId={}, topic={}, exceptionType={}",
								event.requestId(), topic, exception.getClass().getSimpleName());
						return;
					}
					log.info("KafkaAiUsageReportingAdapter : report : AI 사용량 이벤트 발행 완료 - "
									+ "requestId={}, topic={}, partition={}, offset={}",
							event.requestId(), topic, result.getRecordMetadata().partition(),
							result.getRecordMetadata().offset());
				});
	}
}
