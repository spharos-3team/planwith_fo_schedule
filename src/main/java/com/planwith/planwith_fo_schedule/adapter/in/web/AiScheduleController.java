package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleGenerateRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleGenerateResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleGenerateResponse.AiScheduleItemResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_schedule.application.port.in.GenerateAiScheduleUseCase;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping({"/schedules/ai", "/api/v1/schedules/ai"})
public class AiScheduleController {

	private final GenerateAiScheduleUseCase generateAiScheduleUseCase;

	public AiScheduleController(GenerateAiScheduleUseCase generateAiScheduleUseCase) {
		this.generateAiScheduleUseCase = generateAiScheduleUseCase;
	}

	@PostMapping("/generate")
	@Operation(
			summary = "AI 일정 초안 생성",
			description = "최초 생성 또는 사용자가 정보를 수정한 뒤 다시 입력한 조건으로 새 AI 일정 초안을 생성합니다. 결과는 DB에 저장하지 않습니다."
	)
	public ResponseEntity<ApiResponse<AiScheduleGenerateResponse>> generate(
			@RequestHeader(value = "X-Member-UUID", required = false) UUID authenticatedMemberUuid,
			@Valid @RequestBody AiScheduleGenerateRequest request
	) {
		return generateDraft(authenticatedMemberUuid, request);
	}

	@PostMapping("/regenerate")
	@Operation(
			summary = "동일 조건으로 AI 일정 다시 생성",
			description = "기존 입력조건을 변경하지 않고 OpenAI를 다시 호출해 새 일정 초안을 반환합니다. 결과는 DB에 저장하지 않습니다."
	)
	public ResponseEntity<ApiResponse<AiScheduleGenerateResponse>> regenerate(
			@RequestHeader(value = "X-Member-UUID", required = false) UUID authenticatedMemberUuid,
			@Valid @RequestBody AiScheduleGenerateRequest request
	) {
		return generateDraft(authenticatedMemberUuid, request);
	}

	private ResponseEntity<ApiResponse<AiScheduleGenerateResponse>> generateDraft(
			UUID authenticatedMemberUuid,
			AiScheduleGenerateRequest request
	) {
		GenerateAiScheduleUseCase.AiScheduleResult result = generateAiScheduleUseCase.generate(
				AiScheduleGenerateRequestMapper.toCommand(authenticatedMemberUuid, request)
		);
		AiScheduleGenerateResponse response = new AiScheduleGenerateResponse(
				result.memberUuid(),
				result.title(),
				result.destination(),
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
						.toList()
		);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
