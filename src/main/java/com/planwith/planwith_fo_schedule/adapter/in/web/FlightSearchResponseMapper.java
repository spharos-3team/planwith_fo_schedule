package com.planwith.planwith_fo_schedule.adapter.in.web;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSearchResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSearchResponse.AirportScheduleResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSearchResponse.FlightCandidateResponse;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;
import com.planwith.planwith_fo_schedule.application.model.FlightRecommendation;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase.FlightSearchResult;

final class FlightSearchResponseMapper {

	private FlightSearchResponseMapper() {
	}

	static FlightSearchResponse toResponse(FlightSearchResult result) {
		return new FlightSearchResponse(
				result.tripType(),
				result.outboundCandidates().stream().map(FlightSearchResponseMapper::toCandidateResponse).toList(),
				result.returnCandidates().stream().map(FlightSearchResponseMapper::toCandidateResponse).toList()
		);
	}

	static FlightSearchResponse toResponse(FlightRecommendation recommendation) {
		return new FlightSearchResponse(
				recommendation.tripType(),
				recommendation.outboundCandidates().stream()
						.map(FlightSearchResponseMapper::toCandidateResponse)
						.toList(),
				recommendation.returnCandidates().stream()
						.map(FlightSearchResponseMapper::toCandidateResponse)
						.toList()
		);
	}

	private static FlightCandidateResponse toCandidateResponse(FlightCandidate candidate) {
		return new FlightCandidateResponse(
				candidate.flightDate(),
				candidate.flightStatus(),
				toAirportResponse(candidate.departure()),
				toAirportResponse(candidate.arrival()),
				candidate.carrierCode(),
				candidate.flightNumber(),
				candidate.operatingCarrierCode(),
				candidate.aircraftCode(),
				candidate.durationMinutes()
		);
	}

	private static AirportScheduleResponse toAirportResponse(FlightCandidate.AirportSchedule airport) {
		return new AirportScheduleResponse(
				airport.airportCode(),
				airport.terminal(),
				airport.gate(),
				airport.scheduledAt(),
				airport.timezone()
		);
	}
}
