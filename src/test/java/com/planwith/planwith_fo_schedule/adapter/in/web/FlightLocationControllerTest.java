package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.planwith.planwith_fo_schedule.application.exception.FlightLocationNotSupportedException;
import com.planwith.planwith_fo_schedule.application.port.in.GetFlightLocationUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.GetFlightLocationUseCase.FlightLocationResult;

class FlightLocationControllerTest {

	private GetFlightLocationUseCase getFlightLocationUseCase;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		getFlightLocationUseCase = mock(GetFlightLocationUseCase.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new FlightLocationController(getFlightLocationUseCase))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void returnsAirportCodesForLocation() throws Exception {
		when(getFlightLocationUseCase.getAirportCodes("서울"))
				.thenReturn(new FlightLocationResult("서울", List.of("ICN", "GMP")));

		mockMvc.perform(get("/api/v1/flight-locations/airports").param("location", "서울"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.location").value("서울"))
				.andExpect(jsonPath("$.data.airportCodes[0]").value("ICN"))
				.andExpect(jsonPath("$.data.airportCodes[1]").value("GMP"));
	}

	@Test
	void returnsNotFoundForUnsupportedLocation() throws Exception {
		when(getFlightLocationUseCase.getAirportCodes("런던"))
				.thenThrow(new FlightLocationNotSupportedException("런던"));

		mockMvc.perform(get("/api/v1/flight-locations/airports").param("location", "런던"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("FLIGHT_LOCATION_NOT_SUPPORTED"));
	}

	@Test
	void returnsBadRequestWhenLocationParameterIsMissing() throws Exception {
		mockMvc.perform(get("/api/v1/flight-locations/airports"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
	}
}
