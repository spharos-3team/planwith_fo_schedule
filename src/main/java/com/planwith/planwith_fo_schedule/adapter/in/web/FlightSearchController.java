package com.planwith.planwith_fo_schedule.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSearchRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.FlightSearchResponse;
import com.planwith.planwith_fo_schedule.application.port.in.SearchFlightsUseCase;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping({"/flights", "/api/v1/flights"})
public class FlightSearchController {

	private static final Logger log = LoggerFactory.getLogger(FlightSearchController.class);

	private final SearchFlightsUseCase searchFlightsUseCase;

	public FlightSearchController(SearchFlightsUseCase searchFlightsUseCase) {
		this.searchFlightsUseCase = searchFlightsUseCase;
	}

	// 실제 항공편 운항정보 후보 조회
	@PostMapping("/search")
	@Operation(
			summary = "실제 항공편 운항정보 후보 조회",
			description = "AviationStack 실시간 Flight API를 호출합니다. 왕복은 출국편과 귀국편을 각각 조회합니다. Free 플랜에서는 현재 운항정보만 조회되며, 미래 운항정보는 유료 플랜의 Future Flight API가 필요합니다."
	)
	public ResponseEntity<ApiResponse<FlightSearchResponse>> search(
			@Valid @RequestBody FlightSearchRequest request
	) {
		log.info("FlightSearchController : POSTsearch : 항공편 운항정보 후보 조회 요청 - tripType={}",
				request.tripType());
		SearchFlightsUseCase.FlightSearchResult result = searchFlightsUseCase.search(
				FlightSearchRequestMapper.toCommand(request)
		);
		FlightSearchResponse response = FlightSearchResponseMapper.toResponse(result);
		log.info("FlightSearchController : POSTsearch : 항공편 운항정보 후보 조회 응답 - outboundCount={}, returnCount={}",
				response.outboundCandidates().size(), response.returnCandidates().size());
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
