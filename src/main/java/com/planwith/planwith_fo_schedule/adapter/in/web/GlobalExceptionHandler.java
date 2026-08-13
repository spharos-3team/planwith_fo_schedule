package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_schedule.application.exception.AirportCodeNotFoundException;
import com.planwith.planwith_fo_schedule.application.exception.AiScheduleGenerationException;
import com.planwith.planwith_fo_schedule.application.exception.AuthenticationRequiredException;
import com.planwith.planwith_fo_schedule.application.exception.FlightLocationNotSupportedException;
import com.planwith.planwith_fo_schedule.application.exception.FlightSearchException;
import com.planwith.planwith_fo_schedule.application.exception.InvalidFlightSearchException;
import com.planwith.planwith_fo_schedule.application.exception.InvalidFlightLocationException;
import com.planwith.planwith_fo_schedule.application.exception.ScheduleAccessDeniedException;
import com.planwith.planwith_fo_schedule.application.exception.ScheduleNotFoundException;
import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage())
		);
		log.warn("GlobalExceptionHandler : handleValidation : 요청값 검증 실패 - fieldCount={}, fields={}",
				fieldErrors.size(), fieldErrors.keySet());
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_REQUEST", "요청값이 올바르지 않습니다.", fieldErrors)
		);
	}

	@ExceptionHandler(InvalidScheduleException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidSchedule(InvalidScheduleException exception) {
		log.warn("GlobalExceptionHandler : handleInvalidSchedule : 일정 비즈니스 규칙 위반 - message={}",
				exception.getMessage());
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(
				ApiResponse.failure("INVALID_SCHEDULE", exception.getMessage(), Map.of())
		);
	}

	@ExceptionHandler(ScheduleNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleScheduleNotFound(ScheduleNotFoundException exception) {
		log.warn("GlobalExceptionHandler : handleScheduleNotFound : 일정 조회 실패 - scheduleUuid={}",
				exception.scheduleUuid());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
				ApiResponse.failure("SCHEDULE_NOT_FOUND", "일정을 찾을 수 없습니다.", Map.of())
		);
	}

	@ExceptionHandler(AuthenticationRequiredException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthenticationRequired(
			AuthenticationRequiredException exception
	) {
		log.warn("GlobalExceptionHandler : handleAuthenticationRequired : 인증되지 않은 API 요청");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
				ApiResponse.failure("AUTHENTICATION_REQUIRED", exception.getMessage(), Map.of())
		);
	}

	@ExceptionHandler(ScheduleAccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleScheduleAccessDenied(
			ScheduleAccessDeniedException exception
	) {
		log.warn("GlobalExceptionHandler : handleScheduleAccessDenied : 일정 접근 권한 없음 - scheduleUuid={}",
				exception.scheduleUuid());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
				ApiResponse.failure("SCHEDULE_ACCESS_DENIED", "일정에 접근할 권한이 없습니다.", Map.of())
		);
	}

	@ExceptionHandler(AiScheduleGenerationException.class)
	public ResponseEntity<ApiResponse<Void>> handleAiScheduleGeneration(AiScheduleGenerationException exception) {
		log.warn("GlobalExceptionHandler : handleAiScheduleGeneration : AI 일정 생성 실패 - message={}",
				exception.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
				ApiResponse.failure(
						"AI_SCHEDULE_GENERATION_FAILED",
						"AI schedule generation failed. Please try again later.",
						Map.of()
				)
		);
	}

	@ExceptionHandler(InvalidFlightLocationException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidFlightLocation(
			InvalidFlightLocationException exception
	) {
		log.warn("GlobalExceptionHandler : handleInvalidFlightLocation : 항공편 지역 입력값 누락");
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_FLIGHT_LOCATION", "지역명을 입력해야 합니다.", Map.of())
		);
	}

	@ExceptionHandler(FlightLocationNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleFlightLocationNotSupported(
			FlightLocationNotSupportedException exception
	) {
		log.warn("GlobalExceptionHandler : handleFlightLocationNotSupported : 지원하지 않는 항공편 지역 - location={}",
				exception.location());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
				ApiResponse.failure("FLIGHT_LOCATION_NOT_SUPPORTED", "지원하지 않는 지역입니다.", Map.of())
		);
	}

	@ExceptionHandler(AirportCodeNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleAirportCodeNotFound(
			AirportCodeNotFoundException exception
	) {
		log.warn("GlobalExceptionHandler : handleAirportCodeNotFound : 유효한 공항 IATA 코드 없음 - location={}",
				exception.location());
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(
				ApiResponse.failure("AIRPORT_CODE_NOT_FOUND", "유효한 공항 코드를 찾을 수 없습니다.", Map.of())
		);
	}

	@ExceptionHandler(InvalidFlightSearchException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidFlightSearch(InvalidFlightSearchException exception) {
		log.warn("GlobalExceptionHandler : handleInvalidFlightSearch : 항공편 검색 조건 오류 - message={}",
				exception.getMessage());
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_FLIGHT_SEARCH", exception.getMessage(), Map.of())
		);
	}

	@ExceptionHandler(FlightSearchException.class)
	public ResponseEntity<ApiResponse<Void>> handleFlightSearch(FlightSearchException exception) {
		log.warn("GlobalExceptionHandler : handleFlightSearch : 외부 항공편 조회 실패 - providerCode={}, message={}",
				exception.providerCode(), exception.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
				ApiResponse.failure(
						"FLIGHT_SEARCH_FAILED",
						"항공편 정보를 조회하지 못했습니다. 잠시 후 다시 시도해 주세요.",
						Map.of()
				)
		);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		log.warn("GlobalExceptionHandler : handleTypeMismatch : 요청 파라미터 타입 불일치 - parameter={}",
				exception.getName());
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_REQUEST", "요청값이 올바르지 않습니다.", Map.of())
		);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnreadableRequest(HttpMessageNotReadableException exception) {
		log.warn("GlobalExceptionHandler : handleUnreadableRequest : 요청 본문 파싱 실패");
		log.debug("GlobalExceptionHandler : handleUnreadableRequest : 요청 본문 파싱 실패 원인", exception);
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_REQUEST", "Request body contains an unsupported value.", Map.of())
		);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingRequestParameter(
			MissingServletRequestParameterException exception
	) {
		log.warn("GlobalExceptionHandler : handleMissingRequestParameter : 필수 요청 파라미터 누락 - parameter={}",
				exception.getParameterName());
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_REQUEST", "Required request parameter is missing.", Map.of())
		);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
		log.error("GlobalExceptionHandler : handleUnexpectedException : 예상하지 못한 시스템 오류 발생", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
				ApiResponse.failure("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", Map.of())
		);
	}
}
