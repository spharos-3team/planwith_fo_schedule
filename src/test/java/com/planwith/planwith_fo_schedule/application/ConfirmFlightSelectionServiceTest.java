package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.exception.AuthenticationRequiredException;
import com.planwith.planwith_fo_schedule.application.exception.FlightCandidateNotFoundException;
import com.planwith.planwith_fo_schedule.application.exception.InvalidFlightSearchException;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.port.in.ConfirmFlightSelectionUseCase.ConfirmFlightSelectionCommand;
import com.planwith.planwith_fo_schedule.application.port.out.FlightSearchPort;
import com.planwith.planwith_fo_schedule.application.port.out.FlightSearchPort.FlightSearchCriteria;
import com.planwith.planwith_fo_schedule.domain.FlightTripType;

class ConfirmFlightSelectionServiceTest {

	private static final UUID MEMBER_UUID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
	private static final LocalDate FLIGHT_DATE = LocalDate.of(2026, 8, 14);

	private FlightSearchPort flightSearchPort;
	private ConfirmFlightSelectionService service;

	@BeforeEach
	void setUp() {
		flightSearchPort = mock(FlightSearchPort.class);
		service = new ConfirmFlightSelectionService(
				flightSearchPort,
				Clock.fixed(Instant.parse("2026-08-14T01:00:00Z"), ZoneOffset.UTC)
		);
	}

	@Test
	void confirmsUnchangedSelectedFlightWithLatestInformation() {
		FlightCandidate selected = candidate("ICN", "NRT", "KE", "703", "09:00", "11:30");
		FlightSearchCriteria criteria = criteria("ICN", "NRT", "KE", "703");
		when(flightSearchPort.search(criteria)).thenReturn(List.of(selected));

		var result = service.confirm(command(selected, null, FlightTripType.ONE_WAY, true));

		assertThat(result.outboundFlight().candidate()).isEqualTo(selected);
		assertThat(result.outboundFlight().refreshed()).isTrue();
		assertThat(result.outboundFlight().informationChanged()).isFalse();
		assertThat(result.confirmedAt()).isEqualTo(OffsetDateTime.parse("2026-08-14T01:00:00Z"));
	}

	@Test
	void returnsLatestFlightAndMarksInformationAsChanged() {
		FlightCandidate selected = candidate("ICN", "NRT", "KE", "703", "09:00", "11:30");
		FlightCandidate latest = candidate("ICN", "NRT", "KE", "703", "09:30", "12:00");
		when(flightSearchPort.search(criteria("ICN", "NRT", "KE", "703")))
				.thenReturn(List.of(latest));

		var result = service.confirm(command(selected, null, FlightTripType.ONE_WAY, true));

		assertThat(result.outboundFlight().candidate()).isEqualTo(latest);
		assertThat(result.outboundFlight().informationChanged()).isTrue();
	}

	@Test
	void confirmsSelectedInformationWithoutExternalCallWhenRefreshIsDisabled() {
		FlightCandidate selected = candidate("ICN", "NRT", "KE", "703", "09:00", "11:30");

		var result = service.confirm(command(selected, null, FlightTripType.ONE_WAY, false));

		assertThat(result.outboundFlight().candidate()).isEqualTo(selected);
		assertThat(result.outboundFlight().refreshed()).isFalse();
		verify(flightSearchPort, never()).search(criteria("ICN", "NRT", "KE", "703"));
	}

	@Test
	void rejectsSelectionWhenSameFlightIsNoLongerAvailable() {
		FlightCandidate selected = candidate("ICN", "NRT", "KE", "703", "09:00", "11:30");
		when(flightSearchPort.search(criteria("ICN", "NRT", "KE", "703")))
				.thenReturn(List.of(candidate("ICN", "NRT", "KE", "705", "10:00", "12:30")));

		assertThatThrownBy(() -> service.confirm(command(selected, null, FlightTripType.ONE_WAY, true)))
				.isInstanceOf(FlightCandidateNotFoundException.class);
	}

	@Test
	void confirmsOutboundAndReverseReturnFlightForRoundTrip() {
		FlightCandidate outbound = candidate("ICN", "NRT", "KE", "703", "09:00", "11:30");
		FlightCandidate inbound = candidate("NRT", "ICN", "KE", "704", "18:00", "20:30");
		when(flightSearchPort.search(criteria("ICN", "NRT", "KE", "703")))
				.thenReturn(List.of(outbound));
		when(flightSearchPort.search(criteria("NRT", "ICN", "KE", "704")))
				.thenReturn(List.of(inbound));

		var result = service.confirm(command(outbound, inbound, FlightTripType.ROUND_TRIP, true));

		assertThat(result.returnFlight().candidate()).isEqualTo(inbound);
		verify(flightSearchPort).search(criteria("ICN", "NRT", "KE", "703"));
		verify(flightSearchPort).search(criteria("NRT", "ICN", "KE", "704"));
	}

	@Test
	void rejectsMissingAuthenticationAndInvalidRoundTrip() {
		FlightCandidate outbound = candidate("ICN", "NRT", "KE", "703", "09:00", "11:30");

		assertThatThrownBy(() -> service.confirm(new ConfirmFlightSelectionCommand(
				null, FlightTripType.ONE_WAY, outbound, null, false
		))).isInstanceOf(AuthenticationRequiredException.class);
		assertThatThrownBy(() -> service.confirm(command(outbound, null, FlightTripType.ROUND_TRIP, false)))
				.isInstanceOf(InvalidFlightSearchException.class);
	}

	private ConfirmFlightSelectionCommand command(
			FlightCandidate outbound,
			FlightCandidate inbound,
			FlightTripType tripType,
			boolean refresh
	) {
		return new ConfirmFlightSelectionCommand(MEMBER_UUID, tripType, outbound, inbound, refresh);
	}

	private FlightSearchCriteria criteria(String departure, String arrival, String carrier, String number) {
		return new FlightSearchCriteria(departure, arrival, FLIGHT_DATE, carrier, number);
	}

	private FlightCandidate candidate(
			String departure,
			String arrival,
			String carrier,
			String number,
			String departureTime,
			String arrivalTime
	) {
		return new FlightCandidate(
				FLIGHT_DATE, "scheduled",
				new FlightCandidate.AirportSchedule(
						departure, "1", "10", OffsetDateTime.parse(FLIGHT_DATE + "T" + departureTime + ":00+09:00"),
						"Asia/Seoul"
				),
				new FlightCandidate.AirportSchedule(
						arrival, "2", "20", OffsetDateTime.parse(FLIGHT_DATE + "T" + arrivalTime + ":00+09:00"),
						"Asia/Tokyo"
				),
				carrier, number, null, "B789", 150L
		);
	}
}
