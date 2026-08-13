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
import com.planwith.planwith_fo_schedule.application.model.FlightRecommendation;
import com.planwith.planwith_fo_schedule.application.port.in.RecommendFlightsUseCase;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping({"/flights", "/api/v1/flights"})
public class FlightRecommendationController {

	private static final Logger log = LoggerFactory.getLogger(FlightRecommendationController.class);

	private final RecommendFlightsUseCase recommendFlightsUseCase;

	public FlightRecommendationController(RecommendFlightsUseCase recommendFlightsUseCase) {
		this.recommendFlightsUseCase = recommendFlightsUseCase;
	}

	// 추천 항공편 TOP 3 조회
	@PostMapping("/recommendations")
	@Operation(
			summary = "추천 항공편 TOP 3 조회",
			description = "직항 여부, 비행시간, 출발시간, 도착시간 순으로 정렬한 추천 항공편을 방향별 최대 3개 반환합니다. 같은 검색조건은 Redis에 10분간 임시 저장합니다."
	)
	public ResponseEntity<ApiResponse<FlightSearchResponse>> recommend(
			@Valid @RequestBody FlightSearchRequest request
	) {
		log.info("FlightRecommendationController : POSTrecommend : 추천 항공편 TOP 3 조회 요청 - tripType={}",
				request.tripType());
		FlightRecommendation recommendation = recommendFlightsUseCase.recommend(
				FlightRecommendationRequestMapper.toCommand(request)
		);
		FlightSearchResponse response = FlightSearchResponseMapper.toResponse(recommendation);
		log.info("FlightRecommendationController : POSTrecommend : 추천 항공편 TOP 3 조회 응답 - outboundCount={}, returnCount={}",
				response.outboundCandidates().size(), response.returnCandidates().size());
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
