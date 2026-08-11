package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.ApiResponse;
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
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_REQUEST", "요청값이 올바르지 않습니다.", fieldErrors)
		);
	}

	@ExceptionHandler(InvalidScheduleException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidSchedule(InvalidScheduleException exception) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(
				ApiResponse.failure("INVALID_SCHEDULE", exception.getMessage(), Map.of())
		);
	}

	@ExceptionHandler(ScheduleNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleScheduleNotFound(ScheduleNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
				ApiResponse.failure("SCHEDULE_NOT_FOUND", "일정을 찾을 수 없습니다.", Map.of())
		);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_REQUEST", "요청값이 올바르지 않습니다.", Map.of())
		);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingRequestParameter(
			MissingServletRequestParameterException exception
	) {
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_REQUEST", "Required request parameter is missing.", Map.of())
		);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
		log.error("Unexpected request processing failure", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
				ApiResponse.failure("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", Map.of())
		);
	}
}
