package com.planwith.planwith_fo_schedule.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.CreateScheduleRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.CreateScheduleResponse;
import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase.CreateScheduleCommand;
import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase.CreateScheduleItemCommand;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

	private final CreateScheduleUseCase createScheduleUseCase;

	public ScheduleController(CreateScheduleUseCase createScheduleUseCase) {
		this.createScheduleUseCase = createScheduleUseCase;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<CreateScheduleResponse>> createSchedule(
			@Valid @RequestBody CreateScheduleRequest request
	) {
		CreateScheduleCommand command = new CreateScheduleCommand(
				request.memberUuid(),
				request.title(),
				request.destination(),
				request.startDate(),
				request.endDate(),
				request.headcount(),
				request.expectedCost(),
				request.transportation(),
				request.content(),
				request.calendarColor(),
				request.creatorType(),
				request.items().stream()
						.map(item -> new CreateScheduleItemCommand(
								item.dayNumber(),
								item.scheduleTime(),
								item.subtitle(),
								item.scheduleType(),
								item.description(),
								item.estimatedCost(),
								item.placeName(),
								item.placeAddress(),
								item.latitude(),
								item.longitude()
						))
						.toList()
		);
		CreateScheduleUseCase.CreateScheduleResult result = createScheduleUseCase.createSchedule(command);
		CreateScheduleResponse response = new CreateScheduleResponse(
				result.scheduleUuid(),
				result.memberUuid(),
				result.title(),
				result.itemCount()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}
}
