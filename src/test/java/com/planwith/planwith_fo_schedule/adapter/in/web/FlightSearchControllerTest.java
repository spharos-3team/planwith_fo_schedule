package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.planwith.planwith_fo_schedule.application.exception.FlightSearchException;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase.FlightSearchCommand;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase.FlightSearchResult;
import com.planwith.planwith_fo_schedule.domain.TripType;

class FlightSearchControllerTest {

	private SearchFlightsUseCase searchFlightsUseCase;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		searchFlightsUseCase = mock(SearchFlightsUseCase.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new FlightSearchController(searchFlightsUseCase))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void returnsOutboundAndReturnCandidates() throws Exception {
		FlightCandidate outbound = candidate("ICN", "NRT", "703");
		FlightCandidate inbound = candidate("NRT", "ICN", "704");
		when(searchFlightsUseCase.search(any())).thenReturn(
				new FlightSearchResult(TripType.ROUND_TRIP, List.of(outbound), List.of(inbound))
		);

		mockMvc.perform(post("/api/v1/flights/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "departureAirportCode": "ICN",
								  "arrivalAirportCode": "NRT",
								  "departureDate": "2026-08-20",
								  "returnDate": "2026-08-22",
								  "tripType": "ROUND_TRIP"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.tripType").value("ROUND_TRIP"))
				.andExpect(jsonPath("$.data.outboundCandidates[0].departure.airportCode").value("ICN"))
				.andExpect(jsonPath("$.data.outboundCandidates[0].arrival.airportCode").value("NRT"))
				.andExpect(jsonPath("$.data.returnCandidates[0].flightNumber").value("704"));

		ArgumentCaptor<FlightSearchCommand> captor = ArgumentCaptor.forClass(FlightSearchCommand.class);
		verify(searchFlightsUseCase).search(captor.capture());
		assertThat(captor.getValue().tripType()).isEqualTo(TripType.ROUND_TRIP);
	}

	@Test
	void defaultsTripTypeToRoundTripAndValidatesReturnDate() throws Exception {
		mockMvc.perform(post("/api/v1/flights/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "departureAirportCode": "ICN",
								  "arrivalAirportCode": "NRT",
								  "departureDate": "2026-08-20"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.error.fieldErrors.roundTripReturnDatePresent").exists());
	}

	@Test
	void convertsProviderFailureToBadGateway() throws Exception {
		when(searchFlightsUseCase.search(any())).thenThrow(new FlightSearchException("provider failed"));

		mockMvc.perform(post("/api/v1/flights/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "departureAirportCode": "GMP",
								  "arrivalAirportCode": "CJU",
								  "departureDate": "2026-08-20",
								  "tripType": "ONE_WAY"
								}
								"""))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.error.code").value("FLIGHT_SEARCH_FAILED"));
	}

	private FlightCandidate candidate(String departureCode, String arrivalCode, String number) {
		return new FlightCandidate(
				LocalDate.of(2026, 8, 20),
				"scheduled",
				new FlightCandidate.AirportSchedule(
						departureCode, "2", "250", OffsetDateTime.parse("2026-08-20T10:00:00+09:00"),
						"Asia/Seoul"
				),
				new FlightCandidate.AirportSchedule(
						arrivalCode, "1", "40", OffsetDateTime.parse("2026-08-20T12:30:00+09:00"),
						"Asia/Tokyo"
				),
				"KE", number, null, "B77W", 150L
		);
	}
}
