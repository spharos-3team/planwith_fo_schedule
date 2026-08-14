package com.planwith.planwith_fo_schedule.adapter.out.flight;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;

class AviationStackFlightMapperTest {

	private final AviationStackFlightMapper mapper = new AviationStackFlightMapper();

	@Test
	void convertsAviationStackResponseToFlightCandidate() {
		AviationStackFlightsResponse.FlightData response = new AviationStackFlightsResponse.FlightData(
				"2026-09-01",
				"scheduled",
				new AviationStackFlightsResponse.AirportEndpoint(
						"ICN", "2", "250", "2026-09-01T09:00:00+09:00", "Asia/Seoul"
				),
				new AviationStackFlightsResponse.AirportEndpoint(
						"HKG", "1", "40", "2026-09-01T12:30:00+08:00", "Asia/Hong_Kong"
				),
				new AviationStackFlightsResponse.Airline("KE"),
				new AviationStackFlightsResponse.Flight(
						"601", new AviationStackFlightsResponse.Codeshared("CX")
				),
				new AviationStackFlightsResponse.Aircraft("B789")
		);

		FlightCandidate candidate = mapper.toCandidate(response);

		assertThat(candidate.flightDate()).isEqualTo(LocalDate.of(2026, 9, 1));
		assertThat(candidate.flightStatus()).isEqualTo("scheduled");
		assertThat(candidate.departure().airportCode()).isEqualTo("ICN");
		assertThat(candidate.departure().terminal()).isEqualTo("2");
		assertThat(candidate.departure().gate()).isEqualTo("250");
		assertThat(candidate.departure().scheduledAt())
				.isEqualTo(OffsetDateTime.parse("2026-09-01T09:00:00+09:00"));
		assertThat(candidate.arrival().airportCode()).isEqualTo("HKG");
		assertThat(candidate.carrierCode()).isEqualTo("KE");
		assertThat(candidate.flightNumber()).isEqualTo("601");
		assertThat(candidate.operatingCarrierCode()).isEqualTo("CX");
		assertThat(candidate.aircraftCode()).isEqualTo("B789");
		assertThat(candidate.durationMinutes()).isEqualTo(270L);
	}

	@Test
	void leavesInvalidOptionalProviderValuesEmptyForDomainValidation() {
		AviationStackFlightsResponse.FlightData response = new AviationStackFlightsResponse.FlightData(
				"invalid-date", null,
				new AviationStackFlightsResponse.AirportEndpoint("ICN", null, null, "invalid-time", null),
				null, null, null, null
		);

		FlightCandidate candidate = mapper.toCandidate(response);

		assertThat(candidate.flightDate()).isNull();
		assertThat(candidate.departure().scheduledAt()).isNull();
		assertThat(candidate.arrival().airportCode()).isNull();
		assertThat(candidate.durationMinutes()).isNull();
	}
}
