package com.planwith.planwith_fo_schedule.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

@SpringBootTest(properties = {
		"aviationstack.enabled=true",
		"aviationstack.access-key=test-access-key",
		"aviationstack.base-url=https://example.test/v1",
		"aviationstack.connect-timeout-millis=1234",
		"aviationstack.read-timeout-millis=5678"
})
@ActiveProfiles("test")
class AviationStackConfigTest {

	private final AviationStackProperties properties;
	private final RestClient aviationStackRestClient;
	private final RestClient openAiRestClient;

	@Autowired
	AviationStackConfigTest(
			AviationStackProperties properties,
			@Qualifier("aviationStackRestClient") RestClient aviationStackRestClient,
			@Qualifier("openAiRestClient") RestClient openAiRestClient
	) {
		this.properties = properties;
		this.aviationStackRestClient = aviationStackRestClient;
		this.openAiRestClient = openAiRestClient;
	}

	@Test
	void bindsAviationStackConfigurationAndCreatesDedicatedClient() {
		assertThat(properties.isEnabled()).isTrue();
		assertThat(properties.getAccessKey()).isEqualTo("test-access-key");
		assertThat(properties.getBaseUrl()).isEqualTo("https://example.test/v1");
		assertThat(properties.getConnectTimeoutMillis()).isEqualTo(1_234);
		assertThat(properties.getReadTimeoutMillis()).isEqualTo(5_678);
		assertThat(aviationStackRestClient).isNotSameAs(openAiRestClient);
	}
}
