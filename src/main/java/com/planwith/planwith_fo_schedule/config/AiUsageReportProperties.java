package com.planwith.planwith_fo_schedule.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.usage-report")
public class AiUsageReportProperties {

	private boolean enabled;
	private String topic = "planwith.ai-usage.reported";
	private int relayBatchSize = 50;
	private Duration sendTimeout = Duration.ofSeconds(10);
	private Duration initialRetryDelay = Duration.ofSeconds(5);
	private Duration maxRetryDelay = Duration.ofMinutes(5);

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getRelayBatchSize() {
		return relayBatchSize;
	}

	public void setRelayBatchSize(int relayBatchSize) {
		this.relayBatchSize = relayBatchSize;
	}

	public Duration getSendTimeout() {
		return sendTimeout;
	}

	public void setSendTimeout(Duration sendTimeout) {
		this.sendTimeout = sendTimeout;
	}

	public Duration getInitialRetryDelay() {
		return initialRetryDelay;
	}

	public void setInitialRetryDelay(Duration initialRetryDelay) {
		this.initialRetryDelay = initialRetryDelay;
	}

	public Duration getMaxRetryDelay() {
		return maxRetryDelay;
	}

	public void setMaxRetryDelay(Duration maxRetryDelay) {
		this.maxRetryDelay = maxRetryDelay;
	}
}
