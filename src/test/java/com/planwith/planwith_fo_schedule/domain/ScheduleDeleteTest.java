package com.planwith.planwith_fo_schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;

class ScheduleDeleteTest {

	@Test
	void marksScheduleDeletedWithoutChangingIdentity() {
		Schedule schedule = Schedule.create(
				new MemberUuid(UUID.randomUUID()),
				"부산 여행",
				"부산",
				LocalDate.of(2026, 8, 20),
				LocalDate.of(2026, 8, 22),
				new Headcount(2),
				ScheduleCost.zero(),
				null,
				null,
				null,
				CreatorType.SELF,
				List.of()
		);
		LocalDateTime deletionTime = LocalDateTime.of(2026, 8, 11, 6, 0);

		Schedule deleted = schedule.delete(deletionTime);

		assertThat(deleted.deletedAt()).isEqualTo(deletionTime);
		assertThat(deleted.scheduleUuid()).isEqualTo(schedule.scheduleUuid());
		assertThat(deleted.memberUuid()).isEqualTo(schedule.memberUuid());
		assertThat(deleted.creatorType()).isEqualTo(schedule.creatorType());
	}
}
