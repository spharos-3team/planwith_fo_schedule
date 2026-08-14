package com.planwith.planwith_fo_schedule.application.model;

import java.util.List;

import com.planwith.planwith_fo_schedule.domain.TripType;

public record FlightRecommendation(
		TripType tripType,
		List<FlightCandidate> outboundCandidates,
		List<FlightCandidate> returnCandidates
) {
	public FlightRecommendation {
		outboundCandidates = List.copyOf(outboundCandidates);
		returnCandidates = List.copyOf(returnCandidates);
	}
}
