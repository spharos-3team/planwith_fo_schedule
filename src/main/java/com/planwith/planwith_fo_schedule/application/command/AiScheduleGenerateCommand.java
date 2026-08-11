package com.planwith.planwith_fo_schedule.application.command;

import java.util.Objects;

import com.planwith.planwith_fo_schedule.domain.FlightTravelClass;
import com.planwith.planwith_fo_schedule.domain.FlightTripType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;

public record AiScheduleGenerateCommand(
		MemberUuid memberUuid,
		String destination,
		SchedulePeriod period,
		Headcount participantCount,
		ScheduleCost estimatedBudget,
		TransportationType transportation,
		TravelStyle travelStyle,
		String additionalRequest,
		AiScheduleFlightCommand flight
) {

	public AiScheduleGenerateCommand {
		Objects.requireNonNull(memberUuid, "Authenticated member UUID is required.");
		destination = requireText(destination, "Destination is required.");
		Objects.requireNonNull(period, "Schedule period is required.");
		Objects.requireNonNull(participantCount, "Participant count is required.");
		Objects.requireNonNull(estimatedBudget, "Estimated budget is required.");
		additionalRequest = trimToNull(additionalRequest);
	}

	public record AiScheduleFlightCommand(
			String departureLocation,
			String originLocationCode,
			String destinationLocationCode,
			FlightTripType tripType,
			FlightTravelClass travelClass
	) {
		public AiScheduleFlightCommand {
			departureLocation = requireText(departureLocation, "Departure location is required.");
			originLocationCode = requireText(originLocationCode, "Origin location code is required.");
			destinationLocationCode = requireText(
					destinationLocationCode,
					"Destination location code is required."
			);
			tripType = tripType == null ? FlightTripType.ROUND_TRIP : tripType;
			travelClass = travelClass == null ? FlightTravelClass.ECONOMY : travelClass;
		}
	}

	private static String requireText(String value, String message) {
		String trimmed = trimToNull(value);
		if (trimmed == null) {
			throw new IllegalArgumentException(message);
		}
		return trimmed;
	}

	private static String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
