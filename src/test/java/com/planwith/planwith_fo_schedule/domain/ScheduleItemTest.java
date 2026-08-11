package com.planwith.planwith_fo_schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.GeoPoint;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleItemLocation;

class ScheduleItemTest {

	@Test
	void createsScheduleItemWithDayOrderTimeAndLocation() {
		ScheduleItem item = createItem(LocalTime.of(10, 0));

		assertThat(item.scheduleItemId()).isNull();
		assertThat(item.scheduleId()).isNull();
		assertThat(item.day().value()).isEqualTo(1);
		assertThat(item.itemType()).isEqualTo(ScheduleItemType.TOUR);
		assertThat(item.title()).isEqualTo("경복궁 관람");
		assertThat(item.location().placeName()).isEqualTo("경복궁");
		assertThat(item.expectedCost().amount()).isEqualTo(3_000L);
	}

	private ScheduleItem createItem(LocalTime startTime) {
		return ScheduleItem.create(
				new DayNumber(1),
				ScheduleItemType.TOUR,
				"경복궁 관람",
				"경복궁을 관람합니다.",
				new ScheduleItemLocation(
						"경복궁",
						"서울특별시 종로구 사직로 161",
						new GeoPoint(new BigDecimal("37.5796170"), new BigDecimal("126.9770410"))
				),
				startTime,
				ScheduleCost.of(3_000L)
		);
	}
}
