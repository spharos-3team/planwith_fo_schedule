package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSelectionConfirmRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSelectionConfirmResponse;
import com.planwith.planwith_fo_schedule.application.port.in.ConfirmFlightSelectionUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.ConfirmFlightSelectionUseCase.FlightSelectionConfirmation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping({"/flights", "/api/v1/flights"})
public class FlightSelectionController {

	private static final Logger log = LoggerFactory.getLogger(FlightSelectionController.class);

	private final ConfirmFlightSelectionUseCase confirmFlightSelectionUseCase;

	public FlightSelectionController(ConfirmFlightSelectionUseCase confirmFlightSelectionUseCase) {
		this.confirmFlightSelectionUseCase = confirmFlightSelectionUseCase;
	}

	// 선택 항공편 최종 정보 확인
	@PostMapping("/confirmations")
	@Operation(
			summary = "선택 항공편 최종 정보 확인",
			description = "추천 결과에서 선택한 항공편을 확정합니다. 기본적으로 AviationStack에서 동일 운항편의 최신 정보를 조회하며, 가격 확인과 DB 저장은 수행하지 않습니다."
	)
	public ResponseEntity<ApiResponse<FlightSelectionConfirmResponse>> confirm(
			@RequestHeader(value = "X-Member-UUID", required = false) UUID memberUuid,
			@Valid @RequestBody FlightSelectionConfirmRequest request
	) {
		log.info("FlightSelectionController : POSTconfirm : 선택 항공편 최종 정보 확인 요청 - tripType={}, refresh={}",
				request.tripType(), request.refreshLatestInformation());
		FlightSelectionConfirmation confirmation = confirmFlightSelectionUseCase.confirm(
				FlightSelectionConfirmMapper.toCommand(memberUuid, request)
		);
		FlightSelectionConfirmResponse response = FlightSelectionConfirmMapper.toResponse(confirmation);
		log.info("FlightSelectionController : POSTconfirm : 선택 항공편 최종 정보 확인 응답 - outboundChanged={}, returnChanged={}",
				response.outboundFlight().informationChanged(),
				response.returnFlight() != null && response.returnFlight().informationChanged());
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
