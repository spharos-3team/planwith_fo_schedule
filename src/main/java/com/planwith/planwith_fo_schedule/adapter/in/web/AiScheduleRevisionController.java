package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleReviseRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleReviseResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiUsageResultResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_schedule.application.port.in.ReviseScheduleWithAiUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.ReviseScheduleWithAiUseCase.ReviseScheduleResult;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping({"/schedules", "/api/v1/schedules"})
public class AiScheduleRevisionController {

	private static final Logger log = LoggerFactory.getLogger(AiScheduleRevisionController.class);

	private final ReviseScheduleWithAiUseCase reviseScheduleWithAiUseCase;

	public AiScheduleRevisionController(ReviseScheduleWithAiUseCase reviseScheduleWithAiUseCase) {
		this.reviseScheduleWithAiUseCase = reviseScheduleWithAiUseCase;
	}

	// 기존 일정 AI 첨삭 초안 생성
	@PostMapping("/{scheduleUuid}/ai/revise")
	@Operation(
			summary = "기존 일정 AI 첨삭",
			description = "기존 일정 전체를 참고해 자유 일정 내용의 수정안만 반환합니다. 결과는 DB에 저장하지 않습니다."
	)
	public ResponseEntity<ApiResponse<AiScheduleReviseResponse>> revise(
			@RequestHeader(value = "X-Member-UUID", required = false) UUID authenticatedMemberUuid,
			@PathVariable UUID scheduleUuid,
			@Valid @RequestBody AiScheduleReviseRequest request
	) {
		log.info("AiScheduleRevisionController : POSTrevise : 기존 일정 AI 첨삭 요청 - scheduleUuid={}",
				scheduleUuid);
		ReviseScheduleResult result = reviseScheduleWithAiUseCase.revise(
				AiScheduleReviseRequestMapper.toCommand(authenticatedMemberUuid, scheduleUuid, request)
		);
		AiScheduleReviseResponse response = new AiScheduleReviseResponse(
				result.scheduleUuid(),
				result.revisedContent(),
				AiUsageResultResponse.from(result.usage())
		);
		log.info("AiScheduleRevisionController : POSTrevise : 기존 일정 AI 첨삭 초안 반환 완료 - scheduleUuid={}",
				scheduleUuid);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
