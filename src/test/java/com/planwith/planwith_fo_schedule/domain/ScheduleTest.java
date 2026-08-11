package com.planwith.planwith_fo_schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.domain.vo.DayNumber;
import com.planwith.planwith_fo_schedule.domain.vo.GeoPoint;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleItemLocation;

class ScheduleTest {

	@Test
	void createsScheduleAggregateFromSqlDomainFields() {
		UUID memberUuid = UUID.randomUUID();
		ScheduleItem item = createScheduleItem();

		Schedule schedule = Schedule.create(
				new MemberUuid(memberUuid),
				"서울 여행",
				"서울",
				LocalDate.of(2026, 8, 10),
				LocalDate.of(2026, 8, 12),
				new Headcount(2),
				ScheduleCost.of(300_000L),
				"대중교통",
				"여름 휴가",
				"#3366FF",
				CreatorType.SELF,
				List.of(item)
		);

		assertThat(schedule.scheduleId()).isNull();
		assertThat(schedule.scheduleUuid().value()).isNotNull();
		assertThat(schedule.memberUuid().value()).isEqualTo(memberUuid);
		assertThat(schedule.title()).isEqualTo("서울 여행");
		assertThat(schedule.destination()).isEqualTo("서울");
		assertThat(schedule.period().numberOfDays()).isEqualTo(3);
		assertThat(schedule.headcount().value()).isEqualTo(2);
		assertThat(schedule.expectedCost().amount()).isEqualTo(300_000L);
		assertThat(schedule.creatorType()).isEqualTo(CreatorType.SELF);
		assertThat(schedule.createdAt()).isNull();
		assertThat(schedule.updatedAt()).isNull();
		assertThat(schedule.items()).containsExactly(item);
	}

	@Test
	void protectsScheduleItemsFromExternalMutation() {
		List<ScheduleItem> items = new ArrayList<>();
		Schedule schedule = Schedule.create(
				new MemberUuid(UUID.randomUUID()),
				"서울 여행",
				"서울",
				LocalDate.of(2026, 8, 10),
				LocalDate.of(2026, 8, 10),
				Headcount.defaultValue(),
				ScheduleCost.unspecified(),
				null,
				null,
				null,
				CreatorType.AI,
				items
		);

		items.add(createScheduleItem());

		assertThat(schedule.items()).isEmpty();
	}

	@Test
	void enumValuesMatchDatabaseEnumValues() {
		assertThat(CreatorType.values()).containsExactly(
				CreatorType.AI,
				CreatorType.SELF,
				CreatorType.SHARED
		);
		assertThat(ScheduleItemType.values()).containsExactly(
				ScheduleItemType.MOVE,
				ScheduleItemType.FOOD,
				ScheduleItemType.TOUR,
				ScheduleItemType.STAY,
				ScheduleItemType.ACTIVITY,
				ScheduleItemType.ETC
		);
		assertThat(FlightTripType.values()).containsExactly(
				FlightTripType.ONE_WAY,
				FlightTripType.ROUND_TRIP
		);
		assertThat(FlightTravelClass.values()).containsExactly(
				FlightTravelClass.ECONOMY,
				FlightTravelClass.PREMIUM_ECONOMY,
				FlightTravelClass.BUSINESS,
				FlightTravelClass.FIRST
		);
		assertThat(FlightDirection.values()).containsExactly(
				FlightDirection.OUTBOUND,
				FlightDirection.RETURN
		);
	}

	private ScheduleItem createScheduleItem() {
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
				LocalTime.of(10, 30),
				ScheduleCost.of(3_000L)
		);
	}
}
