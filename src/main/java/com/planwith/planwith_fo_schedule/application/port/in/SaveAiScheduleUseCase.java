package com.planwith.planwith_fo_schedule.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.TripType;
import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;

public interface SaveAiScheduleUseCase {

	SaveAiScheduleResult save(SaveAiScheduleCommand command);

	record SaveAiScheduleCommand(
			UUID memberUuid,
			String title,
			String destination,
			LocalDate startDate,
			LocalDate endDate,
			Integer participantCount,
			Long estimatedBudget,
			TransportationType transportation,
			TravelStyle travelStyle,
			String content,
			String calendarColor,
			List<SaveAiScheduleItemCommand> items,
			SaveAiScheduleFlightCommand flight
	) {
		public SaveAiScheduleCommand {
			items = items == null ? null : List.copyOf(items);
		}
	}

	record SaveAiScheduleFlightCommand(
			String departureLocation,
			TripType tripType,
			List<FlightCandidate> outboundCandidates,
			List<FlightCandidate> returnCandidates
	) {
		public SaveAiScheduleFlightCommand {
			outboundCandidates = outboundCandidates == null ? null : List.copyOf(outboundCandidates);
			returnCandidates = returnCandidates == null ? List.of() : List.copyOf(returnCandidates);
		}
	}

	record SaveAiScheduleItemCommand(
			Integer dayNumber,
			LocalTime scheduleTime,
			String subtitle,
			ScheduleItemType scheduleType,
			String description,
			Long estimatedCost,
			String placeName,
			String placeAddress,
			BigDecimal latitude,
			BigDecimal longitude
	) {
	}

	record SaveAiScheduleResult(
			UUID scheduleUuid,
			UUID memberUuid,
			String title,
			int itemCount,
			boolean flightSaved,
			int flightSegmentCount
	) {
	}
}
