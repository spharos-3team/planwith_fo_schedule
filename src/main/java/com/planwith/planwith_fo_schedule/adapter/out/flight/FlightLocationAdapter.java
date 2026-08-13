package com.planwith.planwith_fo_schedule.adapter.out.flight;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_schedule.application.port.out.FlightLocationPort;

@Component
public class FlightLocationAdapter implements FlightLocationPort {

	private static final Map<String, List<String>> AIRPORT_CODES_BY_LOCATION = Map.ofEntries(
			Map.entry("서울", List.of("ICN", "GMP")),
			Map.entry("seoul", List.of("ICN", "GMP")),
			Map.entry("부산", List.of("PUS")),
			Map.entry("busan", List.of("PUS")),
			Map.entry("제주", List.of("CJU")),
			Map.entry("jeju", List.of("CJU")),
			Map.entry("도쿄", List.of("NRT", "HND")),
			Map.entry("tokyo", List.of("NRT", "HND")),
			Map.entry("오사카", List.of("KIX", "ITM")),
			Map.entry("osaka", List.of("KIX", "ITM")),
			Map.entry("후쿠오카", List.of("FUK")),
			Map.entry("fukuoka", List.of("FUK"))
	);

	@Override
	public Optional<List<String>> findAirportCodes(String location) {
		return Optional.ofNullable(AIRPORT_CODES_BY_LOCATION.get(location));
	}
}
