package com.planwith.planwith_fo_schedule.adapter.in.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.planwith.planwith_fo_schedule.domain.TripType;
import com.planwith.planwith_fo_schedule.domain.TransportationType;
import com.planwith.planwith_fo_schedule.domain.TravelStyle;

class AiScheduleGenerateRequestSerializationTest {

	private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

	@Test
	void serializesProviderIndependentPublicInputContract() throws Exception {
		AiScheduleGenerateRequest request = new AiScheduleGenerateRequest(
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
						TripType.ROUND_TRIP
				)
		);

		String json = objectMapper.writeValueAsString(request);

		assertThat(json)
				.contains("\"destination\":\"오사카\"")
				.contains("\"participantCount\":2")
				.contains("\"estimatedBudget\":1000000")
				.contains("\"transportation\":\"TRAIN_PUBLIC_TRANSIT\"")
				.contains("\"travelStyle\":\"TOUR_LANDMARK\"")
				.contains("\"originLocationCode\":\"ICN\"")
				.contains("\"tripType\":\"ROUND_TRIP\"")
				.doesNotContain("memberUuid", "provider", "travelClass", "travelPeriodValid");

		AiScheduleGenerateRequest restored = objectMapper.readValue(json, AiScheduleGenerateRequest.class);
		assertThat(restored).isEqualTo(request);
	}
}
