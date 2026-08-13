package com.planwith.planwith_fo_schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.planwith.planwith_fo_schedule.config.AviationStackProperties;
import com.planwith.planwith_fo_schedule.config.DeployProperties;
import com.planwith.planwith_fo_schedule.config.FlightRecommendationCacheProperties;
import com.planwith.planwith_fo_schedule.config.OpenAiProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		DeployProperties.class,
		OpenAiProperties.class,
		AviationStackProperties.class,
		FlightRecommendationCacheProperties.class
})
public class PlanwithFoScheduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoScheduleApplication.class, args);
	}

}
