package com.planwith.planwith_fo_schedule.adapter.out.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.planwith.planwith_fo_schedule.application.model.AiOperationType;
import com.planwith.planwith_fo_schedule.application.model.AiUsageReportEvent;
import com.planwith.planwith_fo_schedule.config.AiUsageReportProperties;

class KafkaAiUsageReportingAdapterTest {

	@Test
	void publishesUsageEventUsingRequestIdAsMessageKey() {
		KafkaTemplate<String, AiUsageReportEvent> kafkaTemplate = kafkaTemplate();
		AiUsageReportProperties properties = new AiUsageReportProperties();
		properties.setTopic("planwith.ai-usage.reported");
		KafkaAiUsageReportingAdapter adapter = new KafkaAiUsageReportingAdapter(kafkaTemplate, properties);
		UUID requestId = UUID.randomUUID();
		AiUsageReportEvent event = event(requestId);
		CompletableFuture<SendResult<String, AiUsageReportEvent>> future = new CompletableFuture<>();
		when(kafkaTemplate.send(
				eq("planwith.ai-usage.reported"),
				eq(requestId.toString()),
				same(event)
		)).thenReturn(future);

		adapter.publish(event);

		verify(kafkaTemplate).send(
				"planwith.ai-usage.reported",
				requestId.toString(),
				event
		);
	}

	@Test
	void rejectsBlankTopicConfiguration() {
		AiUsageReportProperties properties = new AiUsageReportProperties();
		properties.setTopic(" ");

		assertThatThrownBy(() -> new KafkaAiUsageReportingAdapter(kafkaTemplate(), properties))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("topic");
	}

	@SuppressWarnings("unchecked")
	private KafkaTemplate<String, AiUsageReportEvent> kafkaTemplate() {
		return mock(KafkaTemplate.class);
	}

	private AiUsageReportEvent event(UUID requestId) {
		return new AiUsageReportEvent(
				UUID.randomUUID(),
				requestId,
				AiOperationType.GENERATE,
				"gpt-4o-mini",
				100,
				50,
				150,
				Instant.parse("2026-08-17T05:00:00Z")
		);
	}
}
