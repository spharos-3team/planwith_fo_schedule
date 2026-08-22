package com.planwith.planwith_fo_schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.planwith.planwith_fo_schedule.config.AiTokenPolicyProperties;
import com.planwith.planwith_fo_schedule.config.AiUsageReportProperties;
import com.planwith.planwith_fo_schedule.config.AviationStackProperties;
import com.planwith.planwith_fo_schedule.config.DeployProperties;
import com.planwith.planwith_fo_schedule.config.FlightRecommendationCacheProperties;
import com.planwith.planwith_fo_schedule.config.OpenAiProperties;
import com.planwith.planwith_fo_schedule.config.OpenApiProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
		DeployProperties.class,
		OpenApiProperties.class,
		OpenAiProperties.class,
		AviationStackProperties.class,
		FlightRecommendationCacheProperties.class,
		AiUsageReportProperties.class,
		AiTokenPolicyProperties.class
})
public class PlanwithFoScheduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoScheduleApplication.class, args);
	}

}
