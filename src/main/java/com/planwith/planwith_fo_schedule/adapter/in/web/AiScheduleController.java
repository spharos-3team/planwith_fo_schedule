package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleGenerateRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleGenerateResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleGenerateResponse.AiScheduleItemResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleSaveRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleSaveResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiUsageResultResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.OpenAiUsageResponse;
import com.planwith.planwith_fo_schedule.application.model.AiOperationType;
import com.planwith.planwith_fo_schedule.application.port.in.GenerateAiScheduleUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.SaveAiScheduleUseCase;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping({"/schedules/ai", "/api/v1/schedules/ai"})
public class AiScheduleController {
	private static final Logger log = LoggerFactory.getLogger(AiScheduleController.class);

	private final GenerateAiScheduleUseCase generateAiScheduleUseCase;
	private final SaveAiScheduleUseCase saveAiScheduleUseCase;

	public AiScheduleController(
			GenerateAiScheduleUseCase generateAiScheduleUseCase,
			SaveAiScheduleUseCase saveAiScheduleUseCase
	) {
		this.generateAiScheduleUseCase = generateAiScheduleUseCase;
		this.saveAiScheduleUseCase = saveAiScheduleUseCase;
	}

	// AI 일정 초안 생성 및 정보 재입력 후 생성
	@PostMapping("/generate")
	@Operation(
			summary = "AI 일정 초안 생성",
			description = "최초 생성 또는 사용자가 정보를 수정한 뒤 다시 입력한 조건으로 새 AI 일정 초안을 생성합니다. 결과는 DB에 저장하지 않습니다."
	)
	public ResponseEntity<ApiResponse<AiScheduleGenerateResponse>> generate(
			@RequestHeader(value = "X-Auth-User-Id", required = false) UUID authenticatedMemberUuid,
			@Valid @RequestBody AiScheduleGenerateRequest request
	) {
		log.info("AiScheduleController : POSTgenerate : AI 일정 초안 생성 시작");
		logAiDraftRequest(request);
		return generateDraft(
				"POSTgenerate",
				"AI 일정 초안 생성",
				AiOperationType.GENERATE,
				authenticatedMemberUuid,
				request
		);
	}

	// 동일 조건으로 AI 일정 초안 다시 생성
	@PostMapping("/regenerate")
	@Operation(
			summary = "동일 조건으로 AI 일정 다시 생성",
			description = "기존 입력조건을 변경하지 않고 OpenAI를 다시 호출해 새 일정 초안을 반환합니다. 결과는 DB에 저장하지 않습니다."
	)
	public ResponseEntity<ApiResponse<AiScheduleGenerateResponse>> regenerate(
			@RequestHeader(value = "X-Auth-User-Id", required = false) UUID authenticatedMemberUuid,
			@Valid @RequestBody AiScheduleGenerateRequest request
	) {
		log.info("AiScheduleController : POSTregenerate : 동일 조건 AI 일정 초안 재생성 시작");
		logAiDraftRequest(request);
		return generateDraft(
				"POSTregenerate",
				"동일 조건 AI 일정 초안 재생성",
				AiOperationType.REGENERATE,
				authenticatedMemberUuid,
				request
		);
	}

	// 확인한 AI 일정 초안과 선택 항공편을 내 캘린더에 저장
	@PostMapping("/save")
	@Operation(
			summary = "AI 일정 초안 및 선택 항공편 저장",
			description = "사용자가 확인한 AI 일정과 선택한 항공편을 내 캘린더에 저장합니다. "
					+ "항공편을 선택하지 않으면 Schedule과 ScheduleItem만 저장합니다."
	)
	public ResponseEntity<ApiResponse<AiScheduleSaveResponse>> save(
			@RequestHeader(value = "X-Auth-User-Id", required = false) UUID authenticatedMemberUuid,
			@Valid @RequestBody AiScheduleSaveRequest request
	) {
		log.info("AiScheduleController : POSTsave : AI 일정 초안 저장 시작");
		log.debug("AiScheduleController : POSTsave : AI 일정 저장 요청 상세 - startDate={}, endDate={}, "
				+ "participantCount={}, itemCount={}",
				request.startDate(), request.endDate(), request.participantCount(), request.items().size());
		SaveAiScheduleUseCase.SaveAiScheduleResult result = saveAiScheduleUseCase.save(
				AiScheduleSaveRequestMapper.toCommand(authenticatedMemberUuid, request)
		);
		AiScheduleSaveResponse response = new AiScheduleSaveResponse(
				result.scheduleUuid(),
				result.memberUuid(),
				result.title(),
				result.itemCount(),
				result.flightSaved(),
				result.flightSegmentCount()
		);
		log.info("AiScheduleController : POSTsave : AI 일정 초안 저장 완료 - scheduleUuid={}, itemCount={}, "
				+ "flightSaved={}, flightSegmentCount={}",
				result.scheduleUuid(), result.itemCount(), result.flightSaved(), result.flightSegmentCount());
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	private ResponseEntity<ApiResponse<AiScheduleGenerateResponse>> generateDraft(
			String methodName,
			String roleDescription,
			AiOperationType operationType,
			UUID authenticatedMemberUuid,
			AiScheduleGenerateRequest request
	) {
		log.trace("AiScheduleController : {} : {} 요청을 애플리케이션 명령으로 변환", methodName, roleDescription);
		GenerateAiScheduleUseCase.AiScheduleResult result = generateAiScheduleUseCase.generate(
				AiScheduleGenerateRequestMapper.toCommand(authenticatedMemberUuid, request),
				operationType
		);
		AiScheduleGenerateResponse response = new AiScheduleGenerateResponse(
				result.memberUuid(),
				result.title(),
				result.destination(),
				result.imageUrl(),
				result.startDate(),
				result.endDate(),
				result.participantCount(),
				result.estimatedBudget(),
				result.transportation(),
				result.travelStyle(),
				result.content(),
				result.items().stream()
						.map(item -> new AiScheduleItemResponse(
								item.dayNumber(), item.scheduleTime(), item.subtitle(), item.scheduleType(),
								item.description(), item.estimatedCost(), item.placeName(), item.placeAddress(),
								item.latitude(), item.longitude()
						))
						.toList(),
				OpenAiUsageResponse.from(result.scheduleUsage()),
				OpenAiUsageResponse.from(result.imageUsage()),
				AiUsageResultResponse.from(result.usage())
		);
		log.info("AiScheduleController : {} : {} 완료 - itemCount={}",
				methodName, roleDescription, result.items().size());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	private void logAiDraftRequest(AiScheduleGenerateRequest request) {
		log.debug("AiScheduleController : logAiDraftRequest : AI 일정 초안 요청 상세 - startDate={}, "
				+ "endDate={}, participantCount={}, transportation={}, "
				+ "travelStyle={}, flightIncluded={}",
				request.startDate(), request.endDate(), request.participantCount(), request.transportation(),
				request.travelStyle(), request.flight() != null);
	}
}
