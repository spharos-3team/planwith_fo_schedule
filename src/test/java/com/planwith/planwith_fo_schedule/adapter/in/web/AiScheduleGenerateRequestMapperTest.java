package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleFlightRequest;
import com.planwith.planwith_fo_schedule.adapter.in.web.dto.AiScheduleGenerateRequest;
import com.planwith.planwith_fo_schedule.application.exception.AuthenticationRequiredException;
import com.planwith.planwith_fo_schedule.domain.FlightTravelClass;
import com.planwith.planwith_fo_schedule.domain.FlightTripType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

class AiScheduleGenerateRequestMapperTest {

	@Test
	void mapsValidatedRequestAndAuthenticatedMemberToCommand() {
		UUID memberUuid = UUID.randomUUID();
		AiScheduleGenerateRequest request = new AiScheduleGenerateRequest(
				"  오사카  ",
				LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 9, 5),
				2,
				1_000_000L,
				TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.FOOD_TOUR,
				"  맛집을 포함해 주세요.  ",
				new AiScheduleFlightRequest("인천", "ICN", "KIX", null, null)
		);

		var command = AiScheduleGenerateRequestMapper.toCommand(memberUuid, request);

		assertThat(command.memberUuid().value()).isEqualTo(memberUuid);
		assertThat(command.destination()).isEqualTo("오사카");
		assertThat(command.period().startDate()).isEqualTo(LocalDate.of(2026, 9, 1));
		assertThat(command.period().endDate()).isEqualTo(LocalDate.of(2026, 9, 5));
		assertThat(command.participantCount().value()).isEqualTo(2);
		assertThat(command.estimatedBudget().amount()).isEqualTo(1_000_000L);
		assertThat(command.transportation()).isEqualTo(TransportationType.TRAIN_PUBLIC_TRANSIT);
		assertThat(command.travelStyle()).isEqualTo(TravelStyle.FOOD_TOUR);
		assertThat(command.additionalRequest()).isEqualTo("맛집을 포함해 주세요.");
		assertThat(command.flight().tripType()).isEqualTo(FlightTripType.ROUND_TRIP);
		assertThat(command.flight().travelClass()).isEqualTo(FlightTravelClass.ECONOMY);
	}

	@Test
	void rejectsCommandCreationWithoutAuthenticatedMember() {
		AiScheduleGenerateRequest request = new AiScheduleGenerateRequest(
				"오사카",
				LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 9, 5),
				2,
				1_000_000L,
				null,
				null,
				null,
				null
		);

		assertThatThrownBy(() -> AiScheduleGenerateRequestMapper.toCommand(null, request))
				.isInstanceOf(AuthenticationRequiredException.class)
				.hasMessage("Authentication is required.");
	}
}
