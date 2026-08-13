package com.planwith.planwith_fo_schedule.adapter.out.flight;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class FlightLocationAdapterTest {

	private final FlightLocationAdapter adapter = new FlightLocationAdapter();

	@Test
	void returnsActualAirportCodesForSupportedLocations() {
		assertThat(adapter.findAirportCodes("서울")).contains(List.of("ICN", "GMP"));
		assertThat(adapter.findAirportCodes("부산")).contains(List.of("PUS"));
		assertThat(adapter.findAirportCodes("제주")).contains(List.of("CJU"));
		assertThat(adapter.findAirportCodes("도쿄")).contains(List.of("NRT", "HND"));
		assertThat(adapter.findAirportCodes("오사카")).contains(List.of("KIX", "ITM"));
		assertThat(adapter.findAirportCodes("후쿠오카")).contains(List.of("FUK"));
	}

	@Test
	void returnsEnglishAliasesAndEmptyForUnsupportedLocation() {
		assertThat(adapter.findAirportCodes("seoul")).contains(List.of("ICN", "GMP"));
		assertThat(adapter.findAirportCodes("tokyo")).contains(List.of("NRT", "HND"));
		assertThat(adapter.findAirportCodes("런던")).isEmpty();
	}
}
