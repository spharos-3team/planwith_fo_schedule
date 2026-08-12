package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger log = LoggerFactory.getLogger(ScheduleController.class);

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

	// 일반 일정 생성
	@PostMapping
	public ResponseEntity<ApiResponse<CreateScheduleResponse>> createSchedule(
			@Valid @RequestBody CreateScheduleRequest request
	) {
		log.info("ScheduleController : POSTcreateSchedule : 일반 일정 생성 시작");
		log.debug("ScheduleController : POSTcreateSchedule : 일정 생성 요청 상세 - startDate={}, endDate={}, "
				+ "headcount={}, transportation={}, travelStyle={}",
				request.startDate(), request.endDate(), request.headcount(), request.transportation(), request.travelStyle());
		log.trace("ScheduleController : POSTcreateSchedule : 일정 생성 요청을 애플리케이션 명령으로 변환");
		CreateScheduleCommand command = new CreateScheduleCommand(
				request.memberUuid(),
				request.title(),
				request.destination(),
				request.startDate(),
				request.endDate(),
				request.headcount(),
				request.expectedCost(),
				request.transportation(),
				request.travelStyle(),
				request.content(),
				request.calendarColor()
		);
		CreateScheduleUseCase.CreateScheduleResult result = createScheduleUseCase.createSchedule(command);
		CreateScheduleResponse response = new CreateScheduleResponse(
				result.scheduleUuid(),
				result.memberUuid(),
				result.title()
		);
		log.info("ScheduleController : POSTcreateSchedule : 일반 일정 생성 완료 - scheduleUuid={}", result.scheduleUuid());
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	// 일정 상세 조회
	@GetMapping("/{scheduleUuid}")
	public ResponseEntity<ApiResponse<ScheduleDetailResponse>> getScheduleDetail(
			@PathVariable UUID scheduleUuid
	) {
		log.info("ScheduleController : GETgetScheduleDetail : 일정 상세 조회 시작 - scheduleUuid={}", scheduleUuid);
		ScheduleDetailResult result = getScheduleDetailUseCase.getScheduleDetail(scheduleUuid);
		log.trace("ScheduleController : GETgetScheduleDetail : 일정 상세 조회 결과를 응답으로 변환 - scheduleUuid={}",
				scheduleUuid);
		ScheduleDetailResponse response = new ScheduleDetailResponse(
				result.scheduleUuid(),
				result.title(),
				result.destination(),
				result.startDate(),
				result.endDate(),
				result.headcount(),
				result.expectedCost(),
				result.transportation(),
				result.travelStyle(),
				result.content(),
				result.calendarColor(),
				result.creatorType()
		);
		log.debug("ScheduleController : GETgetScheduleDetail : 일정 상세 조회 결과 - scheduleUuid={}, creatorType={}",
				scheduleUuid, result.creatorType());
		log.info("ScheduleController : GETgetScheduleDetail : 일정 상세 조회 완료 - scheduleUuid={}", scheduleUuid);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 기간별 캘린더 일정 조회
	@GetMapping("/calendar")
	public ResponseEntity<ApiResponse<List<CalendarScheduleResponse>>> getCalendarSchedules(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
	) {
		log.info("ScheduleController : GETgetCalendarSchedules : 기간별 캘린더 일정 조회 시작");
		log.debug("ScheduleController : GETgetCalendarSchedules : 캘린더 조회 기간 - startDate={}, endDate={}",
				startDate, endDate);
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
		log.info("ScheduleController : GETgetCalendarSchedules : 기간별 캘린더 일정 조회 완료 - resultCount={}",
				response.size());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 일정 수정
	@PatchMapping("/{scheduleUuid}")
	public ResponseEntity<ApiResponse<UpdateScheduleResponse>> updateSchedule(
			@PathVariable UUID scheduleUuid,
			@Valid @RequestBody UpdateScheduleRequest request
	) {
		log.info("ScheduleController : PATCHupdateSchedule : 일정 수정 시작 - scheduleUuid={}", scheduleUuid);
		log.debug("ScheduleController : PATCHupdateSchedule : 일정 수정 필드 포함 여부 - title={}, destination={}, "
				+ "startDate={}, endDate={}, headcount={}, "
				+ "expectedCost={}, transportation={}, travelStyle={}, content={}, calendarColor={}",
				request.title() != null, request.destination() != null, request.startDate() != null,
				request.endDate() != null, request.headcount() != null, request.expectedCost() != null,
				request.transportation() != null, request.travelStyle() != null, request.content() != null,
				request.calendarColor() != null);
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
						request.travelStyle(),
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
				result.travelStyle(),
				result.content(),
				result.calendarColor(),
				result.creatorType()
		);
		log.info("ScheduleController : PATCHupdateSchedule : 일정 수정 완료 - scheduleUuid={}", result.scheduleUuid());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 일정 삭제(소프트 삭제)
	@DeleteMapping("/{scheduleUuid}")
	public ResponseEntity<Void> deleteSchedule(@PathVariable UUID scheduleUuid) {
		log.info("ScheduleController : DELETEdeleteSchedule : 일정 소프트 삭제 시작 - scheduleUuid={}", scheduleUuid);
		deleteScheduleUseCase.deleteSchedule(scheduleUuid);
		log.info("ScheduleController : DELETEdeleteSchedule : 일정 소프트 삭제 완료 - scheduleUuid={}", scheduleUuid);
		return ResponseEntity.noContent().build();
	}
}
