package com.planwith.planwith_fo_schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.application.port.out.CalendarScheduleQueryPort;
import com.planwith.planwith_fo_schedule.application.port.out.CalendarScheduleQueryPort.CalendarScheduleData;
import com.planwith.planwith_fo_schedule.domain.ScheduleCreatorType;
import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;

class GetCalendarSchedulesServiceTest {

	@Test
	void returnsCalendarSummaryInRepositoryOrder() {
		CalendarScheduleQueryPort queryPort = mock(CalendarScheduleQueryPort.class);
		GetCalendarSchedulesService service = new GetCalendarSchedulesService(queryPort);
		SchedulePeriod period = new SchedulePeriod(
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 31)
		);
		UUID scheduleUuid = UUID.randomUUID();
		when(queryPort.findOverlappingSchedules(period)).thenReturn(List.of(
				new CalendarScheduleData(
						scheduleUuid,
						"오사카 여행",
						LocalDate.of(2026, 8, 10),
						LocalDate.of(2026, 8, 13),
						"#3366FF",
						ScheduleCreatorType.USER
				)
		));

		var result = service.getCalendarSchedules(period.startDate(), period.endDate());

		assertThat(result).hasSize(1);
		assertThat(result.get(0).scheduleUuid()).isEqualTo(scheduleUuid);
		assertThat(result.get(0).title()).isEqualTo("오사카 여행");
		assertThat(result.get(0).creatorType()).isEqualTo(ScheduleCreatorType.USER);
		verify(queryPort).findOverlappingSchedules(period);
	}

	@Test
	void rejectsCalendarPeriodWhenStartDateIsAfterEndDate() {
		CalendarScheduleQueryPort queryPort = mock(CalendarScheduleQueryPort.class);
		GetCalendarSchedulesService service = new GetCalendarSchedulesService(queryPort);

		assertThatThrownBy(() -> service.getCalendarSchedules(
				LocalDate.of(2026, 8, 31),
				LocalDate.of(2026, 8, 1)
		)).isInstanceOf(InvalidScheduleException.class);
	}
}
