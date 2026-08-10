package com.planwith.planwith_fo_schedule.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CreateScheduleUseCase {

	CreateScheduleResult createSchedule(CreateScheduleCommand command);

	record CreateScheduleCommand(UUID ownerId, String title, List<CreateScheduleItemCommand> items) {
		public CreateScheduleCommand {
			items = List.copyOf(items);
		}
	}

	record CreateScheduleItemCommand(String title, Instant startsAt, Instant endsAt) {
	}

	record CreateScheduleResult(UUID scheduleId, UUID ownerId, String title, int itemCount) {
	}
}
