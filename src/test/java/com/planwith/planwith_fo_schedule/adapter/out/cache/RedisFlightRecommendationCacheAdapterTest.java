package com.planwith.planwith_fo_schedule.adapter.out.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.model.FlightRecommendation;
import com.planwith.planwith_fo_schedule.application.model.FlightRecommendationCacheKey;
import com.planwith.planwith_fo_schedule.config.FlightRecommendationCacheProperties;
import com.planwith.planwith_fo_schedule.domain.TripType;

class RedisFlightRecommendationCacheAdapterTest {

	private StringRedisTemplate redisTemplate;
	private ValueOperations<String, String> valueOperations;
	private ObjectMapper objectMapper;
	private RedisFlightRecommendationCacheAdapter adapter;
	private FlightRecommendationCacheKey key;
	private FlightRecommendation recommendation;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		redisTemplate = mock(StringRedisTemplate.class);
		valueOperations = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		objectMapper = new ObjectMapper().findAndRegisterModules();
		FlightRecommendationCacheProperties properties = new FlightRecommendationCacheProperties();
		adapter = new RedisFlightRecommendationCacheAdapter(redisTemplate, objectMapper, properties);
		key = new FlightRecommendationCacheKey(
				"ICN", "NRT", LocalDate.of(2026, 8, 13), null, TripType.ONE_WAY
		);
		recommendation = new FlightRecommendation(
				TripType.ONE_WAY,
				List.of(new FlightCandidate(
						LocalDate.of(2026, 8, 13), "scheduled",
						new FlightCandidate.AirportSchedule("ICN", null, null, null, null),
						new FlightCandidate.AirportSchedule("NRT", null, null, null, null),
						"KE", "101", null, null, 120L
				)),
				List.of()
		);
	}

	@Test
	void savesRecommendationWithTenMinuteTtl() {
		adapter.save(key, recommendation);

		verify(valueOperations).set(
				org.mockito.ArgumentMatchers.eq("flight:recommendation:ICN:NRT:2026-08-13:none:ONE_WAY"),
				anyString(),
				org.mockito.ArgumentMatchers.eq(Duration.ofMinutes(10))
		);
	}

	@Test
	void deserializesCachedRecommendation() throws Exception {
		when(valueOperations.get(anyString())).thenReturn(objectMapper.writeValueAsString(recommendation));

		assertThat(adapter.find(key)).contains(recommendation);
	}

	@Test
	void bypassesCacheWhenRedisIsUnavailable() {
		when(valueOperations.get(anyString())).thenThrow(new IllegalStateException("Redis unavailable"));

		assertThat(adapter.find(key)).isEmpty();
	}
}
