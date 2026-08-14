package com.planwith.planwith_fo_schedule.adapter.out.flight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.planwith.planwith_fo_schedule.application.exception.FlightSearchException;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.port.out.FlightSearchPort.FlightSearchCriteria;
import com.planwith.planwith_fo_schedule.config.AviationStackProperties;

class AviationStackFlightAdapterTest {

	private AviationStackProperties properties;
	private MockRestServiceServer server;
	private AviationStackFlightAdapter adapter;

	@BeforeEach
	void setUp() {
		properties = new AviationStackProperties();
		properties.setEnabled(true);
		properties.setAccessKey("test-access-key");
		RestClient.Builder builder = RestClient.builder().baseUrl("https://api.aviationstack.test/v1");
		server = MockRestServiceServer.bindTo(builder).build();
		adapter = new AviationStackFlightAdapter(builder.build(), properties, new AviationStackFlightMapper());
	}

	@Test
	void callsFlightsApiAndConvertsProviderResponseToCandidates() {
		server.expect(requestTo(allOf(
				containsString("/flights"),
				containsString("access_key=test-access-key"),
				containsString("dep_iata=ICN"),
				containsString("arr_iata=NRT"),
				containsString("limit=100"),
				not(containsString("flight_date"))
		)))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("""
						{
						  "data": [
						    {
						      "flight_date": "2026-08-20",
						      "flight_status": "scheduled",
						      "departure": {
						        "iata": "ICN", "terminal": "2", "gate": "250",
						        "scheduled": "2026-08-20T10:00:00+09:00", "timezone": "Asia/Seoul"
						      },
						      "arrival": {
						        "iata": "NRT", "terminal": "1", "gate": "40",
						        "scheduled": "2026-08-20T12:30:00+09:00", "timezone": "Asia/Tokyo"
						      },
						      "airline": {"iata": "KE"},
						      "flight": {"number": "703", "codeshared": {"airline_iata": "JL"}},
						      "aircraft": {"iata": "B77W"}
						    }
						  ]
						}
						""", MediaType.APPLICATION_JSON));

		List<FlightCandidate> candidates = adapter.search(
				new FlightSearchCriteria("ICN", "NRT", LocalDate.of(2026, 8, 20))
		);

		assertThat(candidates).hasSize(1);
		FlightCandidate candidate = candidates.get(0);
		assertThat(candidate.flightDate()).isEqualTo(LocalDate.of(2026, 8, 20));
		assertThat(candidate.flightStatus()).isEqualTo("scheduled");
		assertThat(candidate.departure().airportCode()).isEqualTo("ICN");
		assertThat(candidate.departure().scheduledAt()).isEqualTo(OffsetDateTime.parse("2026-08-20T10:00:00+09:00"));
		assertThat(candidate.arrival().airportCode()).isEqualTo("NRT");
		assertThat(candidate.carrierCode()).isEqualTo("KE");
		assertThat(candidate.flightNumber()).isEqualTo("703");
		assertThat(candidate.operatingCarrierCode()).isEqualTo("JL");
		assertThat(candidate.aircraftCode()).isEqualTo("B77W");
		assertThat(candidate.durationMinutes()).isEqualTo(150L);
		server.verify();
	}

	@Test
	void addsFlightNumberOnlyWhenConfirmingSelectedFlight() {
		server.expect(requestTo(allOf(
				containsString("dep_iata=ICN"),
				containsString("arr_iata=NRT"),
				containsString("flight_number=703")
		)))
				.andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));

		adapter.search(new FlightSearchCriteria(
				"ICN", "NRT", LocalDate.of(2026, 8, 20), "KE", "703"
		));

		server.verify();
	}

	@Test
	void filtersRealtimeResponseByRequestedFlightDate() {
		server.expect(requestTo(not(containsString("flight_date"))))
				.andRespond(withSuccess("""
						{"data":[{"flight_date":"2026-08-19"}]}
						""", MediaType.APPLICATION_JSON));

		assertThat(adapter.search(
				new FlightSearchCriteria("ICN", "NRT", LocalDate.of(2026, 8, 20))
		)).isEmpty();
		server.verify();
	}

	@Test
	void includesProviderErrorCodeWhenAviationStackRejectsRequest() {
		server.expect(requestTo(containsString("/flights")))
				.andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
						.withStatus(HttpStatus.FORBIDDEN)
						.contentType(MediaType.APPLICATION_JSON)
						.body("""
								{"error":{"code":"function_access_restricted","message":"Plan restriction"}}
								"""));

		assertThatThrownBy(() -> adapter.search(
				new FlightSearchCriteria("ICN", "NRT", LocalDate.of(2026, 8, 20))
		))
				.isInstanceOf(FlightSearchException.class)
				.satisfies(exception -> assertThat(((FlightSearchException) exception).providerCode())
						.isEqualTo("function_access_restricted"));
		server.verify();
	}

	@Test
	void returnsEmptyCandidatesWhenProviderHasNoData() {
		server.expect(requestTo(containsString("/flights")))
				.andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));

		assertThat(adapter.search(
				new FlightSearchCriteria("GMP", "CJU", LocalDate.of(2026, 8, 20))
		)).isEmpty();
		server.verify();
	}

	@Test
	void rejectsDisabledOrMisconfiguredIntegrationBeforeCallingProvider() {
		properties.setEnabled(false);
		assertThatThrownBy(() -> adapter.search(
				new FlightSearchCriteria("ICN", "NRT", LocalDate.of(2026, 8, 20))
		)).isInstanceOf(FlightSearchException.class);

		properties.setEnabled(true);
		properties.setAccessKey(" ");
		assertThatThrownBy(() -> adapter.search(
				new FlightSearchCriteria("ICN", "NRT", LocalDate.of(2026, 8, 20))
		)).isInstanceOf(FlightSearchException.class);
	}
}
