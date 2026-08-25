package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.planwith.planwith_fo_schedule.application.exception.FlightCandidateNotFoundException;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.port.in.ConfirmFlightSelectionUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.ConfirmFlightSelectionUseCase.ConfirmedFlight;
import com.planwith.planwith_fo_schedule.application.port.in.ConfirmFlightSelectionUseCase.FlightSelectionConfirmation;
import com.planwith.planwith_fo_schedule.domain.TripType;

class FlightSelectionControllerTest {

	private static final UUID MEMBER_UUID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");

	private ConfirmFlightSelectionUseCase useCase;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		useCase = mock(ConfirmFlightSelectionUseCase.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new FlightSelectionController(useCase))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void returnsConfirmedLatestFlightInformation() throws Exception {
		FlightCandidate latest = candidate("09:30", "12:00");
		when(useCase.confirm(any())).thenReturn(new FlightSelectionConfirmation(
				MEMBER_UUID, TripType.ONE_WAY, new ConfirmedFlight(latest, true, true), null,
				OffsetDateTime.parse("2026-08-14T01:00:00Z")
		));

		mockMvc.perform(post("/api/v1/flights/confirmations")
						.header("X-Auth-User-Id", MEMBER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody(true)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.outboundFlight.refreshed").value(true))
				.andExpect(jsonPath("$.data.outboundFlight.informationChanged").value(true))
				.andExpect(jsonPath("$.data.outboundFlight.candidate.flightNumber").value("703"))
				.andExpect(jsonPath("$.data.returnFlight").doesNotExist());
	}

	@Test
	void rejectsRequestWithoutMemberAuthentication() throws Exception {
		mockMvc.perform(post("/api/v1/flights/confirmations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody(false)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
	}

	@Test
	void returnsConflictWhenSelectedFlightIsNotAvailable() throws Exception {
		when(useCase.confirm(any())).thenThrow(new FlightCandidateNotFoundException("KE", "703"));

		mockMvc.perform(post("/api/v1/flights/confirmations")
						.header("X-Auth-User-Id", MEMBER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody(true)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("FLIGHT_CANDIDATE_NOT_AVAILABLE"));
	}

	private String requestBody(boolean refresh) {
		return """
				{
				  "tripType": "ONE_WAY",
				  "refreshLatestInformation": %s,
				  "outboundCandidate": {
				    "flightDate": "2026-08-14",
				    "flightStatus": "scheduled",
				    "departure": {
				      "airportCode": "ICN", "terminal": "1", "gate": "10",
				      "scheduledAt": "2026-08-14T09:00:00+09:00", "timezone": "Asia/Seoul"
				    },
				    "arrival": {
				      "airportCode": "NRT", "terminal": "2", "gate": "20",
				      "scheduledAt": "2026-08-14T11:30:00+09:00", "timezone": "Asia/Tokyo"
				    },
				    "carrierCode": "KE",
				    "flightNumber": "703",
				    "aircraftCode": "B789",
				    "durationMinutes": 150
				  }
				}
				""".formatted(refresh);
	}

	private FlightCandidate candidate(String departureTime, String arrivalTime) {
		LocalDate date = LocalDate.of(2026, 8, 14);
		return new FlightCandidate(
				date, "scheduled",
				new FlightCandidate.AirportSchedule(
						"ICN", "1", "10", OffsetDateTime.parse(date + "T" + departureTime + ":00+09:00"),
						"Asia/Seoul"
				),
				new FlightCandidate.AirportSchedule(
						"NRT", "2", "20", OffsetDateTime.parse(date + "T" + arrivalTime + ":00+09:00"),
						"Asia/Tokyo"
				),
				"KE", "703", null, "B789", 150L
		);
	}
}
