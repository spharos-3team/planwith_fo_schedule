package com.planwith.planwith_fo_schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;

class ScheduleValidationTest {

	private static final LocalDate START_DATE = LocalDate.of(2026, 8, 10);
	private static final LocalDate END_DATE = LocalDate.of(2026, 8, 12);

	@Test
	void validatesDestinationBeforeOtherCreationValues() {
		assertThatThrownBy(() -> createSchedule(null, null, null, null, null, null, ScheduleCreatorType.AI))
				.isInstanceOf(InvalidScheduleException.class)
				.hasMessage("Destination is required.");
	}

	@Test
	void rejectsMissingStartDate() {
		assertThatThrownBy(() -> createSchedule("제주도", "제주 여행", null, END_DATE, null, null, ScheduleCreatorType.AI))
				.isInstanceOf(InvalidScheduleException.class)
				.hasMessage("Start date is required.");
	}

	@Test
	void rejectsMissingEndDate() {
		assertThatThrownBy(() -> createSchedule("제주도", "제주 여행", START_DATE, null, null, null, ScheduleCreatorType.AI))
				.isInstanceOf(InvalidScheduleException.class)
				.hasMessage("End date is required.");
	}

	@Test
	void rejectsEndDateBeforeStartDate() {
		assertThatThrownBy(() -> createSchedule(
				"제주도",
				"제주 여행",
				END_DATE,
				START_DATE,
				null,
				null,
				ScheduleCreatorType.AI
		))
				.isInstanceOf(InvalidScheduleException.class)
				.hasMessage("End date must not be before start date.");
	}

	@Test
	void acceptsSameStartAndEndDate() {
		Schedule schedule = createSchedule(
				"제주도",
				"당일 여행",
				START_DATE,
				START_DATE,
				null,
				null,
				ScheduleCreatorType.AI
		);

		assertThat(schedule.period().numberOfDays()).isEqualTo(1);
	}

	@Test
	void createsAutomaticTitleFromDestinationWhenTitleIsMissing() {
		Schedule schedule = createSchedule("제주도", "  ", START_DATE, END_DATE, null, null, ScheduleCreatorType.AI);

		assertThat(schedule.title()).isEqualTo("제주도 여행");
	}

	@Test
	void appliesDefaultCalendarColorWhenColorIsMissing() {
		Schedule schedule = createSchedule("제주도", "제주 여행", START_DATE, END_DATE, null, " ", ScheduleCreatorType.AI);

		assertThat(schedule.calendarColor()).isEqualTo(Schedule.DEFAULT_CALENDAR_COLOR);
	}

	@Test
	void appliesDefaultHeadcountOnlyToAiSchedule() {
		Schedule schedule = createSchedule("제주도", "제주 여행", START_DATE, END_DATE, null, null, ScheduleCreatorType.AI);

		assertThat(schedule.headcount()).isEqualTo(Headcount.defaultValue());
	}

	@Test
	void requiresHeadcountForNonAiSchedule() {
		assertThatThrownBy(() -> createSchedule(
				"제주도",
				"제주 여행",
				START_DATE,
				END_DATE,
				null,
				null,
				ScheduleCreatorType.USER
		))
				.isInstanceOf(InvalidScheduleException.class)
				.hasMessage("Headcount is required for non-AI schedules.");
	}

	@Test
	void preservesExplicitTitleColorAndHeadcount() {
		Schedule schedule = createSchedule(
				"제주도",
				"가족 여행",
				START_DATE,
				END_DATE,
				new Headcount(4),
				"#3366FF",
				ScheduleCreatorType.USER
		);

		assertThat(schedule.title()).isEqualTo("가족 여행");
		assertThat(schedule.calendarColor()).isEqualTo("#3366FF");
		assertThat(schedule.headcount().value()).isEqualTo(4);
	}

	private Schedule createSchedule(
			String destination,
			String title,
			LocalDate startDate,
			LocalDate endDate,
			Headcount headcount,
			String calendarColor,
			ScheduleCreatorType creatorType
	) {
		return Schedule.create(
				new MemberUuid(UUID.randomUUID()),
				title,
				destination,
				startDate,
				endDate,
				headcount,
				ScheduleCost.unspecified(),
				null,
				null,
				calendarColor,
				creatorType,
				List.of()
		);
	}
}
