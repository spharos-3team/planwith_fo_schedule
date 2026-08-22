package com.planwith.planwith_fo_schedule.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.token-policy")
public class AiTokenPolicyProperties {

	private long openaiTokensPerProductToken = 1_000L;

	public long getOpenaiTokensPerProductToken() {
		return openaiTokensPerProductToken;
	}

	public void setOpenaiTokensPerProductToken(long openaiTokensPerProductToken) {
		this.openaiTokensPerProductToken = openaiTokensPerProductToken;
	}
}
