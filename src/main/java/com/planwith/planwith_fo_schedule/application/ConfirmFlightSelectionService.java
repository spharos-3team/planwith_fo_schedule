package com.planwith.planwith_fo_schedule.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_schedule.application.exception.AuthenticationRequiredException;
import com.planwith.planwith_fo_schedule.application.exception.FlightCandidateNotFoundException;
import com.planwith.planwith_fo_schedule.application.exception.InvalidFlightSearchException;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.port.in.ConfirmFlightSelectionUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.FlightSearchPort;
import com.planwith.planwith_fo_schedule.application.port.out.FlightSearchPort.FlightSearchCriteria;
import com.planwith.planwith_fo_schedule.domain.TripType;

@Service
public class ConfirmFlightSelectionService implements ConfirmFlightSelectionUseCase {

	private static final Logger log = LoggerFactory.getLogger(ConfirmFlightSelectionService.class);

	private final FlightSearchPort flightSearchPort;
	private final Clock clock;

	@Autowired
	public ConfirmFlightSelectionService(FlightSearchPort flightSearchPort) {
		this(flightSearchPort, Clock.systemUTC());
	}

	ConfirmFlightSelectionService(FlightSearchPort flightSearchPort, Clock clock) {
		this.flightSearchPort = flightSearchPort;
		this.clock = clock;
	}

	@Override
	public FlightSelectionConfirmation confirm(ConfirmFlightSelectionCommand command) {
		ConfirmFlightSelectionCommand validatedCommand = validate(command);
		log.info("ConfirmFlightSelectionService : confirm : 선택 항공편 최종 정보 확인 시작 - tripType={}, refresh={}",
				validatedCommand.tripType(), validatedCommand.refreshLatestInformation());

		ConfirmedFlight outboundFlight = confirmCandidate(
				validatedCommand.outboundCandidate(),
				validatedCommand.refreshLatestInformation()
		);
		ConfirmedFlight returnFlight = validatedCommand.returnCandidate() == null
				? null
				: confirmCandidate(
						validatedCommand.returnCandidate(),
						validatedCommand.refreshLatestInformation()
				);

		log.info("ConfirmFlightSelectionService : confirm : 선택 항공편 최종 정보 확인 완료 - outboundChanged={}, returnChanged={}",
				outboundFlight.informationChanged(), returnFlight != null && returnFlight.informationChanged());
		return new FlightSelectionConfirmation(
				validatedCommand.memberUuid(),
				validatedCommand.tripType(),
				outboundFlight,
				returnFlight,
				OffsetDateTime.now(clock)
		);
	}

	private ConfirmedFlight confirmCandidate(FlightCandidate selectedCandidate, boolean refresh) {
		if (!refresh) {
			return new ConfirmedFlight(selectedCandidate, false, false);
		}

		List<FlightCandidate> latestCandidates = flightSearchPort.search(new FlightSearchCriteria(
				selectedCandidate.departure().airportCode(),
				selectedCandidate.arrival().airportCode(),
				selectedCandidate.flightDate(),
				selectedCandidate.carrierCode(),
				selectedCandidate.flightNumber()
		));
		FlightCandidate latestCandidate = latestCandidates.stream()
				.filter(candidate -> isSameFlight(selectedCandidate, candidate))
				.findFirst()
				.orElseThrow(() -> new FlightCandidateNotFoundException(
						selectedCandidate.carrierCode(), selectedCandidate.flightNumber()
				));
		return new ConfirmedFlight(latestCandidate, true, !selectedCandidate.equals(latestCandidate));
	}

	private boolean isSameFlight(FlightCandidate selected, FlightCandidate latest) {
		return Objects.equals(selected.flightDate(), latest.flightDate())
				&& equalsIgnoreCase(selected.carrierCode(), latest.carrierCode())
				&& equalsIgnoreCase(selected.flightNumber(), latest.flightNumber())
				&& latest.departure() != null
				&& latest.arrival() != null
				&& equalsIgnoreCase(selected.departure().airportCode(), latest.departure().airportCode())
				&& equalsIgnoreCase(selected.arrival().airportCode(), latest.arrival().airportCode());
	}

	private ConfirmFlightSelectionCommand validate(ConfirmFlightSelectionCommand command) {
		if (command == null) {
			throw new InvalidFlightSearchException("Flight confirmation command is required.");
		}
		if (command.memberUuid() == null) {
			throw new AuthenticationRequiredException();
		}
		TripType tripType = command.tripType() == null ? TripType.ONE_WAY : command.tripType();
		validateCandidate(command.outboundCandidate(), "Outbound flight");
		if (tripType == TripType.ROUND_TRIP && command.returnCandidate() == null) {
			throw new InvalidFlightSearchException("Return flight is required for a round trip.");
		}
		if (command.returnCandidate() != null) {
			validateCandidate(command.returnCandidate(), "Return flight");
			validateReturnRoute(command.outboundCandidate(), command.returnCandidate());
		}
		return new ConfirmFlightSelectionCommand(
				command.memberUuid(), tripType, command.outboundCandidate(), command.returnCandidate(),
				command.refreshLatestInformation()
		);
	}

	private void validateCandidate(FlightCandidate candidate, String fieldName) {
		if (candidate == null || candidate.flightDate() == null || candidate.departure() == null
				|| candidate.arrival() == null || isBlank(candidate.departure().airportCode())
				|| isBlank(candidate.arrival().airportCode()) || isBlank(candidate.carrierCode())
				|| isBlank(candidate.flightNumber()) || candidate.departure().scheduledAt() == null
				|| candidate.arrival().scheduledAt() == null) {
			throw new InvalidFlightSearchException(fieldName + " information is incomplete.");
		}
	}

	private void validateReturnRoute(FlightCandidate outbound, FlightCandidate inbound) {
		if (!equalsIgnoreCase(outbound.departure().airportCode(), inbound.arrival().airportCode())
				|| !equalsIgnoreCase(outbound.arrival().airportCode(), inbound.departure().airportCode())) {
			throw new InvalidFlightSearchException("Return flight route must be the reverse of outbound route.");
		}
		if (inbound.flightDate().isBefore(outbound.flightDate())) {
			throw new InvalidFlightSearchException("Return flight date must not be before outbound flight date.");
		}
	}

	private boolean equalsIgnoreCase(String left, String right) {
		return normalize(left).equals(normalize(right));
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
