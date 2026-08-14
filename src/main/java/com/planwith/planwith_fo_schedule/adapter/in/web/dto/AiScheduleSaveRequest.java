package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.planwith.planwith_fo_schedule.domain.ScheduleItemType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;
import com.planwith.planwith_fo_schedule.domain.TripType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AiScheduleSaveRequest(
		@NotBlank @Size(max = 200) String title,
		@NotBlank @Size(max = 200) String destination,
		@NotNull LocalDate startDate,
		@NotNull LocalDate endDate,
		@NotNull @Min(1) Integer participantCount,
		@NotNull @PositiveOrZero Long estimatedBudget,
		TransportationType transportation,
		TravelStyle travelStyle,
		@Size(max = 65_535) String content,
		@Size(max = 30) String calendarColor,
		@NotEmpty List<@Valid AiScheduleItemSaveRequest> items,
		@Valid SelectedFlightSaveRequest flight
) {

	@JsonIgnore
	@AssertTrue(message = "startDate must not be after endDate")
	public boolean isTravelPeriodValid() {
		return startDate == null || endDate == null || !startDate.isAfter(endDate);
	}

	public record AiScheduleItemSaveRequest(
			@NotNull @Min(1) Integer dayNumber,
			LocalTime scheduleTime,
			@NotBlank @Size(max = 200) String subtitle,
			@NotNull ScheduleItemType scheduleType,
			@Size(max = 65_535) String description,
			@NotNull @PositiveOrZero Long estimatedCost,
			@Size(max = 200) String placeName,
			@Size(max = 500) String placeAddress,
			@DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
			@DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude
	) {

		@JsonIgnore
		@AssertTrue(message = "latitude and longitude must be provided together")
		public boolean isCoordinatePairValid() {
			return (latitude == null) == (longitude == null);
		}
	}

	public record SelectedFlightSaveRequest(
			@NotBlank @Size(max = 200) String departureLocation,
			@NotNull TripType tripType,
			@NotEmpty List<@Valid FlightCandidateRequest> outboundCandidates,
			List<@Valid FlightCandidateRequest> returnCandidates
	) {
		public SelectedFlightSaveRequest {
			outboundCandidates = outboundCandidates == null ? null : List.copyOf(outboundCandidates);
			returnCandidates = returnCandidates == null ? List.of() : List.copyOf(returnCandidates);
		}

		@JsonIgnore
		@AssertTrue(message = "returnCandidates are required only for a round trip")
		public boolean isReturnCandidatesValid() {
			if (tripType == null || returnCandidates == null) {
				return true;
			}
			return tripType == TripType.ROUND_TRIP ? !returnCandidates.isEmpty() : returnCandidates.isEmpty();
		}
	}
}
