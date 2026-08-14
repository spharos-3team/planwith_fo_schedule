package com.planwith.planwith_fo_schedule.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI planwithFoScheduleOpenApi(OpenApiProperties properties) {
		return new OpenAPI()
				.servers(List.of(new Server().url(properties.serverUrl())))
				.info(new Info()
						.title("PlanWith planwith-fo-schedule API")
						.description("Server notebook deploy verification service")
						.version("v1"));
	}
}
