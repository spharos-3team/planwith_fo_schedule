package com.planwith.planwith_fo_schedule.application;

import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_schedule.application.exception.AiScheduleGenerationException;
import com.planwith.planwith_fo_schedule.application.exception.ScheduleAccessDeniedException;
import com.planwith.planwith_fo_schedule.application.exception.ScheduleNotFoundException;
import com.planwith.planwith_fo_schedule.application.port.in.ReviseScheduleWithAiUseCase;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleRevisionPort;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleRevisionPort.RevisedSchedule;
import com.planwith.planwith_fo_schedule.application.port.out.AiScheduleRevisionPort.ScheduleRevisionContext;
import com.planwith.planwith_fo_schedule.application.port.out.ScheduleRepositoryPort;
import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;
import com.planwith.planwith_fo_schedule.domain.Schedule;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleUuid;

@Service
public class AiScheduleRevisionService implements ReviseScheduleWithAiUseCase {

	private static final Logger log = LoggerFactory.getLogger(AiScheduleRevisionService.class);

	private final ScheduleRepositoryPort scheduleRepositoryPort;
	private final AiScheduleRevisionPort aiScheduleRevisionPort;

	public AiScheduleRevisionService(
			ScheduleRepositoryPort scheduleRepositoryPort,
			AiScheduleRevisionPort aiScheduleRevisionPort
	) {
		this.scheduleRepositoryPort = scheduleRepositoryPort;
		this.aiScheduleRevisionPort = aiScheduleRevisionPort;
	}

	@Override
	public ReviseScheduleResult revise(ReviseScheduleCommand command) {
		ReviseScheduleCommand validatedCommand = Objects.requireNonNull(
				command,
				"AI schedule revision command is required."
		);
		UUID scheduleUuid = Objects.requireNonNull(validatedCommand.scheduleUuid(), "Schedule UUID is required.");
		UUID memberUuid = Objects.requireNonNull(
				validatedCommand.authenticatedMemberUuid(),
				"Authenticated member UUID is required."
		);
		requireAdditionalRequest(validatedCommand.additionalRequest());

		log.info("AiScheduleRevisionService : revise : AI 일정 첨삭 비즈니스 로직 시작 - scheduleUuid={}",
				scheduleUuid);
		Schedule schedule = scheduleRepositoryPort.findByScheduleUuid(new ScheduleUuid(scheduleUuid))
				.orElseThrow(() -> new ScheduleNotFoundException(scheduleUuid));
		validateOwnership(schedule, memberUuid);

		RevisedSchedule revisedSchedule = aiScheduleRevisionPort.revise(toRevisionContext(
				schedule,
				validatedCommand.additionalRequest()
		));
		if (revisedSchedule == null) {
			throw new AiScheduleGenerationException("OpenAI returned no revised schedule.");
		}
		validateRevisedSchedule(revisedSchedule);

		try {
			Schedule revisedDraft = schedule.update(
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					revisedSchedule.content(),
					null
			);
			log.info("AiScheduleRevisionService : revise : AI 일정 첨삭 초안 생성 완료 - scheduleUuid={}",
					scheduleUuid);
			return new ReviseScheduleResult(
					scheduleUuid,
					revisedDraft.content(),
					revisedSchedule.usage()
			);
		} catch (InvalidScheduleException exception) {
			throw new AiScheduleGenerationException("AI returned an invalid schedule revision.", exception);
		}
	}

	private ScheduleRevisionContext toRevisionContext(Schedule schedule, String additionalRequest) {
		return new ScheduleRevisionContext(
				schedule.title(),
				schedule.destination(),
				schedule.period().startDate(),
				schedule.period().endDate(),
				schedule.headcount().value(),
				schedule.expectedCost().amount(),
				schedule.transportation(),
				schedule.travelStyle(),
				schedule.content(),
				additionalRequest
		);
	}

	private void validateOwnership(Schedule schedule, UUID authenticatedMemberUuid) {
		if (!schedule.memberUuid().value().equals(authenticatedMemberUuid)) {
			log.warn("AiScheduleRevisionService : validateOwnership : 일정 첨삭 소유권 검증 실패 - scheduleUuid={}",
					schedule.scheduleUuid().value());
			throw new ScheduleAccessDeniedException(schedule.scheduleUuid().value());
		}
	}

	private void requireAdditionalRequest(String additionalRequest) {
		if (additionalRequest == null || additionalRequest.isBlank()) {
			throw new InvalidScheduleException("Additional revision request is required.");
		}
	}

	private void validateRevisedSchedule(RevisedSchedule revisedSchedule) {
		if (revisedSchedule.content() == null || revisedSchedule.content().isBlank()) {
			throw new AiScheduleGenerationException("OpenAI returned an invalid schedule revision.");
		}
	}
}
