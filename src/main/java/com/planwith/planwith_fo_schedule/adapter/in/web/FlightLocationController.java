package com.planwith.planwith_fo_schedule.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightLocationResponse;
import com.planwith.planwith_fo_schedule.application.port.in.GetFlightLocationUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping({"/flight-locations", "/api/v1/flight-locations"})
public class FlightLocationController {

	private static final Logger log = LoggerFactory.getLogger(FlightLocationController.class);

	private final GetFlightLocationUseCase getFlightLocationUseCase;

	public FlightLocationController(GetFlightLocationUseCase getFlightLocationUseCase) {
		this.getFlightLocationUseCase = getFlightLocationUseCase;
	}

	// 지역별 실제 공항 IATA 코드 조회
	@GetMapping("/airports")
	@Operation(
			summary = "지역별 공항 IATA 코드 조회",
			description = "서울, 도쿄 등의 지역명을 실제 항공편 검색에 사용할 공항 IATA 코드 목록으로 변환합니다."
	)
	public ResponseEntity<ApiResponse<FlightLocationResponse>> getAirportCodes(
			@Parameter(description = "공항을 조회할 지역명", example = "서울")
			@RequestParam String location
	) {
		log.info("FlightLocationController : GETgetAirportCodes : 지역별 공항 코드 조회 요청 - location={}",
				location);
		GetFlightLocationUseCase.FlightLocationResult result = getFlightLocationUseCase.getAirportCodes(location);
		FlightLocationResponse response = new FlightLocationResponse(result.location(), result.airportCodes());
		log.info("FlightLocationController : GETgetAirportCodes : 지역별 공항 코드 조회 응답 - airportCount={}",
				response.airportCodes().size());
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
