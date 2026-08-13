package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.exception.InvalidFlightSearchException;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase.FlightSearchCommand;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase.FlightSearchResult;
import com.planwith.planwith_fo_schedule.application.port.out.FlightSearchPort;
import com.planwith.planwith_fo_schedule.application.port.out.FlightSearchPort.FlightSearchCriteria;
import com.planwith.planwith_fo_schedule.domain.FlightTripType;

class FlightSearchServiceTest {

	private FlightSearchPort flightSearchPort;
	private FlightSearchService service;

	@BeforeEach
	void setUp() {
		flightSearchPort = mock(FlightSearchPort.class);
		service = new FlightSearchService(flightSearchPort);
	}

	@Test
	void searchesOutboundAndReturnFlightsSeparatelyForRoundTrip() {
		LocalDate departureDate = LocalDate.of(2026, 8, 20);
		LocalDate returnDate = LocalDate.of(2026, 8, 22);
		FlightSearchCriteria outboundCriteria = new FlightSearchCriteria("ICN", "NRT", departureDate);
		FlightSearchCriteria returnCriteria = new FlightSearchCriteria("NRT", "ICN", returnDate);
		FlightCandidate outbound = candidate(departureDate, "ICN", "NRT", "100");
		FlightCandidate inbound = candidate(returnDate, "NRT", "ICN", "101");
		when(flightSearchPort.search(outboundCriteria)).thenReturn(List.of(outbound));
		when(flightSearchPort.search(returnCriteria)).thenReturn(List.of(inbound));

		FlightSearchResult result = service.search(new FlightSearchCommand(
				"icn", "nrt", departureDate, returnDate, null
		));

		assertThat(result.tripType()).isEqualTo(FlightTripType.ROUND_TRIP);
		assertThat(result.outboundCandidates()).containsExactly(outbound);
		assertThat(result.returnCandidates()).containsExactly(inbound);
		verify(flightSearchPort).search(outboundCriteria);
		verify(flightSearchPort).search(returnCriteria);
	}

	@Test
	void searchesOnlyOutboundForOneWay() {
		LocalDate departureDate = LocalDate.of(2026, 8, 20);
		FlightSearchCriteria criteria = new FlightSearchCriteria("GMP", "CJU", departureDate);
		when(flightSearchPort.search(criteria)).thenReturn(List.of());

		FlightSearchResult result = service.search(new FlightSearchCommand(
				"GMP", "CJU", departureDate, null, FlightTripType.ONE_WAY
		));

		assertThat(result.returnCandidates()).isEmpty();
		verify(flightSearchPort).search(criteria);
		verify(flightSearchPort, never()).search(new FlightSearchCriteria("CJU", "GMP", departureDate));
	}

	@Test
	void rejectsInvalidSearchConditionsBeforeExternalCall() {
		LocalDate departureDate = LocalDate.of(2026, 8, 20);

		assertThatThrownBy(() -> service.search(new FlightSearchCommand(
				"ICN", "ICN", departureDate, departureDate, FlightTripType.ROUND_TRIP
		))).isInstanceOf(InvalidFlightSearchException.class);
		assertThatThrownBy(() -> service.search(new FlightSearchCommand(
				"ICN", "NRT", departureDate, null, FlightTripType.ROUND_TRIP
		))).isInstanceOf(InvalidFlightSearchException.class);
		assertThatThrownBy(() -> service.search(new FlightSearchCommand(
				"IC", "NRT", departureDate, null, FlightTripType.ONE_WAY
		))).isInstanceOf(InvalidFlightSearchException.class);
	}

	private FlightCandidate candidate(LocalDate date, String departure, String arrival, String number) {
		return new FlightCandidate(
				date, "scheduled",
				new FlightCandidate.AirportSchedule(departure, null, null, null, null),
				new FlightCandidate.AirportSchedule(arrival, null, null, null, null),
				"KE", number, null, null, null
		);
	}
}
