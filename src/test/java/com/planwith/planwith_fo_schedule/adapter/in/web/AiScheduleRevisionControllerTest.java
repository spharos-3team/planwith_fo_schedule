package com.planwith.planwith_fo_schedule.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.planwith.planwith_fo_schedule.application.exception.ScheduleAccessDeniedException;
import com.planwith.planwith_fo_schedule.application.port.in.ReviseScheduleWithAiUseCase;
import com.planwith.planwith_fo_schedule.application.port.in.ReviseScheduleWithAiUseCase.ReviseScheduleCommand;
import com.planwith.planwith_fo_schedule.application.port.in.ReviseScheduleWithAiUseCase.ReviseScheduleResult;

class AiScheduleRevisionControllerTest {

	private ReviseScheduleWithAiUseCase useCase;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		useCase = mock(ReviseScheduleWithAiUseCase.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new AiScheduleRevisionController(useCase))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void returnsAiRevisionDraftForOwnedSchedule() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID scheduleUuid = UUID.randomUUID();
		when(useCase.revise(any())).thenReturn(new ReviseScheduleResult(
				scheduleUuid,
				"부산 바다 여행",
				"해운대를 중심으로 여유롭게 여행합니다."
		));

		mockMvc.perform(post("/api/v1/schedules/{scheduleUuid}/ai/revise", scheduleUuid)
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRequest()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.scheduleUuid").value(scheduleUuid.toString()))
				.andExpect(jsonPath("$.data.revisedTitle").value("부산 바다 여행"))
				.andExpect(jsonPath("$.data.revisedContent").value("해운대를 중심으로 여유롭게 여행합니다."));

		ArgumentCaptor<ReviseScheduleCommand> captor = ArgumentCaptor.forClass(ReviseScheduleCommand.class);
		verify(useCase).revise(captor.capture());
		assertThat(captor.getValue().authenticatedMemberUuid()).isEqualTo(memberUuid);
		assertThat(captor.getValue().scheduleUuid()).isEqualTo(scheduleUuid);
		assertThat(captor.getValue().additionalRequest()).isEqualTo("바다 중심으로 여유롭게 수정해줘");
	}

	@Test
	void rejectsRevisionWithoutAuthenticationHeader() throws Exception {
		mockMvc.perform(post("/api/v1/schedules/{scheduleUuid}/ai/revise", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRequest()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

		verifyNoInteractions(useCase);
	}

	@Test
	void rejectsBlankAdditionalRequest() throws Exception {
		mockMvc.perform(post("/api/v1/schedules/{scheduleUuid}/ai/revise", UUID.randomUUID())
						.header("X-Member-UUID", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"additionalRequest": " "}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

		verifyNoInteractions(useCase);
	}

	@Test
	void returnsForbiddenWhenMemberDoesNotOwnSchedule() throws Exception {
		UUID scheduleUuid = UUID.randomUUID();
		when(useCase.revise(any())).thenThrow(new ScheduleAccessDeniedException(scheduleUuid));

		mockMvc.perform(post("/api/v1/schedules/{scheduleUuid}/ai/revise", scheduleUuid)
						.header("X-Member-UUID", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRequest()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("SCHEDULE_ACCESS_DENIED"));
	}

	private String validRequest() {
		return """
				{"additionalRequest": "바다 중심으로 여유롭게 수정해줘"}
				""";
	}
}
