package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.config.AiTokenPolicyProperties;

class AiProductTokenConverterTest {

	private final AiProductTokenConverter converter = converter(1_000L);

	@Test
	void convertsJapanTripScaleUsageToAboutNineProductTokens() {
		AiProductTokenConverter.ConvertedUsage converted = converter.convert(8_235, 1_201, 9_436);

		assertThat(converted.inputTokens()).isEqualTo(8);
		assertThat(converted.outputTokens()).isEqualTo(1);
		assertThat(converted.totalTokens()).isEqualTo(9);
	}

	@Test
	void roundsHalfUpAndKeepsInputOutputConsistentWithTotal() {
		AiProductTokenConverter.ConvertedUsage converted = converter.convert(2_500, 3_200, 5_700);

		assertThat(converted.inputTokens()).isEqualTo(3);
		assertThat(converted.outputTokens()).isEqualTo(3);
		assertThat(converted.totalTokens()).isEqualTo(6);
	}

	@Test
	void chargesAtLeastOneProductTokenWhenOpenAiUsageExists() {
		AiProductTokenConverter.ConvertedUsage converted = converter.convert(165, 95, 260);

		assertThat(converted.totalTokens()).isEqualTo(1);
		assertThat(converted.inputTokens() + converted.outputTokens()).isEqualTo(1);
	}

	@Test
	void keepsZeroWhenOpenAiUsageIsZero() {
		AiProductTokenConverter.ConvertedUsage converted = converter.convert(0, 0, 0);

		assertThat(converted.inputTokens()).isZero();
		assertThat(converted.outputTokens()).isZero();
		assertThat(converted.totalTokens()).isZero();
	}

	@Test
	void usesConfiguredDivisor() {
		AiProductTokenConverter hundredDivisor = converter(100L);

		assertThat(hundredDivisor.convert(9_400, 0, 9_400).totalTokens()).isEqualTo(94);
	}

	@Test
	void rejectsNonPositiveDivisor() {
		assertThatThrownBy(() -> converter(0L))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("positive");
	}

	private AiProductTokenConverter converter(long openaiTokensPerProductToken) {
		AiTokenPolicyProperties properties = new AiTokenPolicyProperties();
		properties.setOpenaiTokensPerProductToken(openaiTokensPerProductToken);
		return new AiProductTokenConverter(properties);
	}
}
