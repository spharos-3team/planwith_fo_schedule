package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.model.FlightRecommendation;
import com.planwith.planwith_fo_schedule.application.port.in.RecommendFlightsUseCase;
import com.planwith.planwith_fo_schedule.domain.FlightTripType;

class FlightRecommendationControllerTest {

	private RecommendFlightsUseCase useCase;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		useCase = mock(RecommendFlightsUseCase.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new FlightRecommendationController(useCase))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void returnsTopThreeFlightRecommendations() throws Exception {
		when(useCase.recommend(any())).thenReturn(new FlightRecommendation(
				FlightTripType.ONE_WAY,
				List.of(candidate("101"), candidate("102"), candidate("103")),
				List.of()
		));

		mockMvc.perform(post("/api/v1/flights/recommendations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "departureAirportCode": "ICN",
								  "arrivalAirportCode": "NRT",
								  "departureDate": "2026-08-13",
								  "tripType": "ONE_WAY"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.outboundCandidates.length()").value(3))
				.andExpect(jsonPath("$.data.outboundCandidates[0].flightNumber").value("101"))
				.andExpect(jsonPath("$.data.returnCandidates.length()").value(0));

		verify(useCase).recommend(any());
	}

	@Test
	void validatesRoundTripReturnDate() throws Exception {
		mockMvc.perform(post("/api/v1/flights/recommendations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "departureAirportCode": "ICN",
								  "arrivalAirportCode": "NRT",
								  "departureDate": "2026-08-13",
								  "tripType": "ROUND_TRIP"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
	}

	private FlightCandidate candidate(String flightNumber) {
		return new FlightCandidate(
				LocalDate.of(2026, 8, 13), "scheduled",
				new FlightCandidate.AirportSchedule("ICN", null, null, null, null),
				new FlightCandidate.AirportSchedule("NRT", null, null, null, null),
				"KE", flightNumber, null, null, 120L
		);
	}
}
