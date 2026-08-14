package com.planwith.planwith_fo_schedule.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openapi")
public record OpenApiProperties(
		String serverUrl
) {
	public OpenApiProperties {
		if (serverUrl == null || serverUrl.isBlank()) {
			serverUrl = "/";
		}
	}
}
