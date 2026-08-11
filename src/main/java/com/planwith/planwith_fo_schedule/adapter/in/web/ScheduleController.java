package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.CalendarScheduleResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.CreateScheduleRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.CreateScheduleResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ScheduleDetailResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.UpdateScheduleRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.UpdateScheduleResponse;
import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.CreateScheduleUseCase.CreateScheduleCommand;
import com.planwith.planwith_fo_schedule.application.port.in.DeleteScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.GetScheduleDetailUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.GetScheduleDetailUseCase.ScheduleDetailResult;
import com.planwith.planwith_fo_schedule.application.port.in.GetCalendarSchedulesUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.UpdateScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.UpdateScheduleUseCase.UpdateScheduleCommand;
import com.planwith.planwith_fo_schedule.application.port.in.UpdateScheduleUseCase.UpdateScheduleResult;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/schedules", "/api/v1/schedules"})
public class ScheduleController {

	private final CreateScheduleUseCase createScheduleUseCase;
	private final GetScheduleDetailUseCase getScheduleDetailUseCase;
	private final UpdateScheduleUseCase updateScheduleUseCase;
	private final DeleteScheduleUseCase deleteScheduleUseCase;
	private final GetCalendarSchedulesUseCase getCalendarSchedulesUseCase;

	public ScheduleController(
			CreateScheduleUseCase createScheduleUseCase,
			GetScheduleDetailUseCase getScheduleDetailUseCase,
			UpdateScheduleUseCase updateScheduleUseCase,
			DeleteScheduleUseCase deleteScheduleUseCase,
			GetCalendarSchedulesUseCase getCalendarSchedulesUseCase
	) {
		this.createScheduleUseCase = createScheduleUseCase;
		this.getScheduleDetailUseCase = getScheduleDetailUseCase;
		this.updateScheduleUseCase = updateScheduleUseCase;
		this.deleteScheduleUseCase = deleteScheduleUseCase;
		this.getCalendarSchedulesUseCase = getCalendarSchedulesUseCase;
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
				request.calendarColor()
		);
		CreateScheduleUseCase.CreateScheduleResult result = createScheduleUseCase.createSchedule(command);
		CreateScheduleResponse response = new CreateScheduleResponse(
				result.scheduleUuid(),
				result.memberUuid(),
				result.title()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@GetMapping("/{scheduleUuid}")
	public ResponseEntity<ApiResponse<ScheduleDetailResponse>> getScheduleDetail(
			@PathVariable UUID scheduleUuid
	) {
		ScheduleDetailResult result = getScheduleDetailUseCase.getScheduleDetail(scheduleUuid);
		ScheduleDetailResponse response = new ScheduleDetailResponse(
				result.scheduleUuid(),
				result.title(),
				result.destination(),
				result.startDate(),
				result.endDate(),
				result.headcount(),
				result.expectedCost(),
				result.transportation(),
				result.content(),
				result.calendarColor(),
				result.creatorType()
		);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/calendar")
	public ResponseEntity<ApiResponse<List<CalendarScheduleResponse>>> getCalendarSchedules(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
	) {
		List<CalendarScheduleResponse> response = getCalendarSchedulesUseCase
				.getCalendarSchedules(startDate, endDate).stream()
				.map(schedule -> new CalendarScheduleResponse(
						schedule.scheduleUuid(),
						schedule.title(),
						schedule.startDate(),
						schedule.endDate(),
						schedule.calendarColor(),
						schedule.creatorType()
				))
				.toList();
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PatchMapping("/{scheduleUuid}")
	public ResponseEntity<ApiResponse<UpdateScheduleResponse>> updateSchedule(
			@PathVariable UUID scheduleUuid,
			@Valid @RequestBody UpdateScheduleRequest request
	) {
		UpdateScheduleResult result = updateScheduleUseCase.updateSchedule(
				scheduleUuid,
				new UpdateScheduleCommand(
						request.title(),
						request.destination(),
						request.startDate(),
						request.endDate(),
						request.headcount(),
						request.expectedCost(),
						request.transportation(),
						request.content(),
						request.calendarColor()
				)
		);
		UpdateScheduleResponse response = new UpdateScheduleResponse(
				result.scheduleUuid(),
				result.title(),
				result.destination(),
				result.startDate(),
				result.endDate(),
				result.headcount(),
				result.expectedCost(),
				result.transportation(),
				result.content(),
				result.calendarColor(),
				result.creatorType()
		);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@DeleteMapping("/{scheduleUuid}")
	public ResponseEntity<Void> deleteSchedule(@PathVariable UUID scheduleUuid) {
		deleteScheduleUseCase.deleteSchedule(scheduleUuid);
		return ResponseEntity.noContent().build();
	}
}
