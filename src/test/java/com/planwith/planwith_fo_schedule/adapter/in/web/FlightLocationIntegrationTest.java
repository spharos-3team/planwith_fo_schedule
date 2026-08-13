package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlightLocationIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void resolvesSeoulAndTokyoToActualAirportCandidates() throws Exception {
		mockMvc.perform(get("/api/v1/flight-locations/airports").param("location", "서울"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.airportCodes[0]").value("ICN"))
				.andExpect(jsonPath("$.data.airportCodes[1]").value("GMP"));

		mockMvc.perform(get("/api/v1/flight-locations/airports").param("location", "도쿄"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.airportCodes[0]").value("NRT"))
				.andExpect(jsonPath("$.data.airportCodes[1]").value("HND"));
	}
}
