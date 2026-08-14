package com.planwith.planwith_fo_schedule.adapter.in.web;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightCandidateRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightCandidateRequest.AirportScheduleRequest;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate.AirportSchedule;

final class FlightCandidateRequestMapper {

	private FlightCandidateRequestMapper() {
	}

	static FlightCandidate toCandidate(FlightCandidateRequest candidate) {
		if (candidate == null) {
			return null;
		}
		return new FlightCandidate(
				candidate.flightDate(), candidate.flightStatus(),
				toAirportSchedule(candidate.departure()), toAirportSchedule(candidate.arrival()),
				candidate.carrierCode(), candidate.flightNumber(), candidate.operatingCarrierCode(),
				candidate.aircraftCode(), candidate.durationMinutes()
		);
	}

	private static AirportSchedule toAirportSchedule(AirportScheduleRequest schedule) {
		return schedule == null ? null : new AirportSchedule(
				schedule.airportCode(), schedule.terminal(), schedule.gate(), schedule.scheduledAt(), schedule.timezone()
		);
	}
}
