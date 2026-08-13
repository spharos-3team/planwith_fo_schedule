package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.exception.AirportCodeNotFoundException;
import com.planwith.planwith_fo_schedule.application.exception.FlightLocationNotSupportedException;
import com.planwith.planwith_fo_schedule.application.exception.InvalidFlightLocationException;
import com.planwith.planwith_fo_schedule.application.port.in.GetFlightLocationUseCase.FlightLocationResult;
import com.planwith.planwith_fo_schedule.application.port.out.FlightLocationPort;

class GetFlightLocationServiceTest {

	private FlightLocationPort flightLocationPort;
	private GetFlightLocationService service;

	@BeforeEach
	void setUp() {
		flightLocationPort = mock(FlightLocationPort.class);
		service = new GetFlightLocationService(flightLocationPort);
	}

	@Test
	void normalizesLocationAndReturnsValidatedDistinctCodes() {
		when(flightLocationPort.findAirportCodes("tokyo"))
				.thenReturn(Optional.of(List.of("nrt", " HND ", "NRT")));

		FlightLocationResult result = service.getAirportCodes("  ToKyO  ");

		assertThat(result.location()).isEqualTo("tokyo");
		assertThat(result.airportCodes()).containsExactly("NRT", "HND");
	}

	@Test
	void rejectsBlankLocation() {
		assertThatThrownBy(() -> service.getAirportCodes("  "))
				.isInstanceOf(InvalidFlightLocationException.class);
	}

	@Test
	void rejectsUnsupportedLocation() {
		when(flightLocationPort.findAirportCodes("런던")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getAirportCodes("런던"))
				.isInstanceOf(FlightLocationNotSupportedException.class);
	}

	@Test
	void rejectsLocationWithoutValidIataCode() {
		when(flightLocationPort.findAirportCodes("테스트"))
				.thenReturn(Optional.of(List.of("", "AB", "1234")));

		assertThatThrownBy(() -> service.getAirportCodes("테스트"))
				.isInstanceOf(AirportCodeNotFoundException.class);
	}
}
