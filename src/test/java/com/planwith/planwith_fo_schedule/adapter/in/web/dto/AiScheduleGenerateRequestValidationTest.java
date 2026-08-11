package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.domain.FlightTravelClass;
import com.planwith.planwith_fo_schedule.domain.FlightTripType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class AiScheduleGenerateRequestValidationTest {

	private static ValidatorFactory validatorFactory;
	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@AfterAll
	static void closeValidator() {
		validatorFactory.close();
	}

	@Test
	void acceptsValidRequiredAndOptionalInputs() {
		AiScheduleGenerateRequest request = validRequest();

		assertThat(validator.validate(request)).isEmpty();
	}

	@Test
	void rejectsMissingRequiredInputs() {
		AiScheduleGenerateRequest request = new AiScheduleGenerateRequest(
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null
		);

		assertThat(pathsOf(validator.validate(request))).contains(
				"destination",
				"startDate",
				"endDate",
				"participantCount",
				"estimatedBudget"
		);
	}

	@Test
	void rejectsInvalidPeriodParticipantCountAndBudget() {
		AiScheduleGenerateRequest request = new AiScheduleGenerateRequest(
				"오사카",
				LocalDate.of(2026, 9, 5),
				LocalDate.of(2026, 9, 1),
				0,
				-1L,
				null,
				null,
				null,
				null
		);

		assertThat(pathsOf(validator.validate(request))).contains(
				"travelPeriodValid",
				"participantCount",
				"estimatedBudget"
		);
	}

	@Test
	void validatesNestedFlightInputWhenPresent() {
		AiScheduleGenerateRequest request = new AiScheduleGenerateRequest(
				"오사카",
				LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 9, 5),
				2,
				1_000_000L,
				null,
				null,
				null,
				new AiScheduleFlightRequest("인천", "icn", "KIX1", null, null)
		);

		assertThat(pathsOf(validator.validate(request))).contains(
				"flight.originLocationCode",
				"flight.destinationLocationCode"
		);
	}

	private AiScheduleGenerateRequest validRequest() {
		return new AiScheduleGenerateRequest(
				"오사카",
				LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 9, 5),
				2,
				1_000_000L,
				TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.TOUR_LANDMARK,
				"아이와 함께 갈 수 있는 장소를 포함해 주세요.",
				new AiScheduleFlightRequest(
						"인천",
						"ICN",
						"KIX",
						FlightTripType.ROUND_TRIP,
						FlightTravelClass.ECONOMY
				)
		);
	}

	private Set<String> pathsOf(Set<ConstraintViolation<AiScheduleGenerateRequest>> violations) {
		return violations.stream()
				.map(violation -> violation.getPropertyPath().toString())
				.collect(java.util.stream.Collectors.toSet());
	}
}
