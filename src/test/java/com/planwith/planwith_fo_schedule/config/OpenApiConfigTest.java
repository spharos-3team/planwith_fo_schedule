package com.planwith.planwith_fo_schedule.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;

class OpenApiConfigTest {

	private final OpenApiConfig openApiConfig = new OpenApiConfig();

	@Test
	void gatewayRelativeServerUrlIsIncludedInOpenApiDocument() {
		OpenAPI openAPI = openApiConfig.planwithFoScheduleOpenApi(new OpenApiProperties("/"));

		assertThat(openAPI.getServers()).hasSize(1);
		assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("/");
	}

	@Test
	void blankServerUrlUsesGatewayRelativeDefault() {
		OpenAPI openAPI = openApiConfig.planwithFoScheduleOpenApi(new OpenApiProperties(" "));

		assertThat(openAPI.getServers()).hasSize(1);
		assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("/");
	}
}
