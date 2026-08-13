package com.planwith.planwith_fo_schedule.adapter.out.cache;

import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_schedule.application.model.FlightRecommendation;
import com.planwith.planwith_fo_schedule.application.model.FlightRecommendationCacheKey;
import com.planwith.planwith_fo_schedule.application.port.out.FlightRecommendationCachePort;
import com.planwith.planwith_fo_schedule.config.FlightRecommendationCacheProperties;

@Component
public class RedisFlightRecommendationCacheAdapter implements FlightRecommendationCachePort {

	private static final Logger log = LoggerFactory.getLogger(RedisFlightRecommendationCacheAdapter.class);

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final FlightRecommendationCacheProperties properties;

	public RedisFlightRecommendationCacheAdapter(
			StringRedisTemplate redisTemplate,
			ObjectMapper objectMapper,
			FlightRecommendationCacheProperties properties
	) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
		this.properties = properties;
	}

	@Override
	public Optional<FlightRecommendation> find(FlightRecommendationCacheKey key) {
		if (!properties.isEnabled()) {
			return Optional.empty();
		}
		try {
			String cachedValue = redisTemplate.opsForValue().get(toRedisKey(key));
			if (cachedValue == null) {
				return Optional.empty();
			}
			log.debug("RedisFlightRecommendationCacheAdapter : find : 항공편 추천 캐시 조회 성공");
			return Optional.of(objectMapper.readValue(cachedValue, FlightRecommendation.class));
		} catch (JsonProcessingException exception) {
			log.warn("RedisFlightRecommendationCacheAdapter : find : 항공편 추천 캐시 역직렬화 실패");
			return Optional.empty();
		} catch (RuntimeException exception) {
			log.warn("RedisFlightRecommendationCacheAdapter : find : Redis 조회 실패 - exceptionType={}",
					exception.getClass().getSimpleName());
			return Optional.empty();
		}
	}

	@Override
	public void save(FlightRecommendationCacheKey key, FlightRecommendation recommendation) {
		if (!properties.isEnabled()) {
			return;
		}
		try {
			String value = objectMapper.writeValueAsString(recommendation);
			redisTemplate.opsForValue().set(toRedisKey(key), value, validTtl());
			log.debug("RedisFlightRecommendationCacheAdapter : save : 항공편 추천 캐시 저장 완료 - ttlSeconds={}",
					validTtl().toSeconds());
		} catch (JsonProcessingException exception) {
			log.warn("RedisFlightRecommendationCacheAdapter : save : 항공편 추천 캐시 직렬화 실패");
		} catch (RuntimeException exception) {
			log.warn("RedisFlightRecommendationCacheAdapter : save : Redis 저장 실패 - exceptionType={}",
					exception.getClass().getSimpleName());
		}
	}

	private String toRedisKey(FlightRecommendationCacheKey key) {
		return properties.getKeyPrefix() + ":" + key.value();
	}

	private Duration validTtl() {
		Duration ttl = properties.getTtl();
		return ttl == null || ttl.isNegative() || ttl.isZero() ? Duration.ofMinutes(10) : ttl;
	}
}
