package com.planwith.planwith_fo_schedule.application;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_schedule.application.model.AiOperationType;
import com.planwith.planwith_fo_schedule.application.model.AiUsageResult;
import com.planwith.planwith_fo_schedule.application.model.OpenAiUsage;

@Component
public class AiUsageAggregator {

	private static final Logger log = LoggerFactory.getLogger(AiUsageAggregator.class);

	public AiUsageResult aggregate(
			UUID memberUuid,
			UUID requestId,
			AiOperationType operationType,
			List<OpenAiUsage> usages
	) {
		List<OpenAiUsage> validUsages = usages == null
				? List.of()
				: usages.stream().filter(usage -> usage != null).toList();
		if (validUsages.isEmpty()) {
			throw new IllegalArgumentException("At least one OpenAI usage is required.");
		}

		try {
			AiUsageResult result = new AiUsageResult(
					memberUuid,
					requestId,
					operationType,
					aggregateModels(validUsages),
					sumInputTokens(validUsages),
					sumOutputTokens(validUsages),
					sumTotalTokens(validUsages)
			);
			log.info("AiUsageAggregator : aggregate : AI 요청 사용량 집계 완료 - memberUuid={}, requestId={}, "
							+ "operationType={}, model={}, inputTokens={}, outputTokens={}, totalTokens={}",
					result.memberUuid(), result.requestId(), result.operationType(), result.model(),
					result.inputTokens(), result.outputTokens(), result.totalTokens());
			return result;
		} catch (ArithmeticException exception) {
			throw new IllegalArgumentException("OpenAI token usage total exceeds the supported range.", exception);
		}
	}

	private String aggregateModels(List<OpenAiUsage> usages) {
		return usages.stream()
				.map(OpenAiUsage::model)
				.distinct()
				.collect(Collectors.joining(","));
	}

	private long sumInputTokens(List<OpenAiUsage> usages) {
		return usages.stream()
				.mapToLong(OpenAiUsage::inputTokens)
				.reduce(0L, Math::addExact);
	}

	private long sumOutputTokens(List<OpenAiUsage> usages) {
		return usages.stream()
				.mapToLong(OpenAiUsage::outputTokens)
				.reduce(0L, Math::addExact);
	}

	private long sumTotalTokens(List<OpenAiUsage> usages) {
		return usages.stream()
				.mapToLong(OpenAiUsage::totalTokens)
				.reduce(0L, Math::addExact);
	}
}
