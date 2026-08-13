package com.planwith.planwith_fo_schedule.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_schedule.application.model.FlightRecommendation;
import com.planwith.planwith_fo_schedule.application.model.FlightRecommendationCacheKey;

public interface FlightRecommendationCachePort {

	Optional<FlightRecommendation> find(FlightRecommendationCacheKey key);

	void save(FlightRecommendationCacheKey key, FlightRecommendation recommendation);
}
