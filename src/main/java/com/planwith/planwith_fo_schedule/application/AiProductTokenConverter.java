package com.planwith.planwith_fo_schedule.application;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_schedule.config.AiTokenPolicyProperties;

@Component
public class AiProductTokenConverter {

	private final long openaiTokensPerProductToken;

	public AiProductTokenConverter(AiTokenPolicyProperties properties) {
		long divisor = properties.getOpenaiTokensPerProductToken();
		if (divisor <= 0) {
			throw new IllegalArgumentException("openaiTokensPerProductToken must be positive.");
		}
		this.openaiTokensPerProductToken = divisor;
	}

	public ConvertedUsage convert(long inputTokens, long outputTokens, long totalTokens) {
		if (inputTokens < 0 || outputTokens < 0 || totalTokens < 0) {
			throw new IllegalArgumentException("OpenAI token usage cannot be negative.");
		}
		long convertedTotal = convertAmount(totalTokens);
		if (totalTokens > 0 && convertedTotal == 0) {
			convertedTotal = 1L;
		}
		long convertedInput = convertAmount(inputTokens);
		if (convertedInput > convertedTotal) {
			return new ConvertedUsage(convertedTotal, 0L, convertedTotal);
		}
		return new ConvertedUsage(convertedInput, convertedTotal - convertedInput, convertedTotal);
	}

	private long convertAmount(long rawTokens) {
		return BigDecimal.valueOf(rawTokens)
				.divide(BigDecimal.valueOf(openaiTokensPerProductToken), 0, RoundingMode.HALF_UP)
				.longValueExact();
	}

	public record ConvertedUsage(long inputTokens, long outputTokens, long totalTokens) {
	}
}
