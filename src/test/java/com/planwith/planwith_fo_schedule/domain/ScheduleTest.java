package com.planwith.planwith_fo_schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
				TransportationType.TRAIN_PUBLIC_TRANSIT,
				TravelStyle.TOUR_LANDMARK,
				"여름 휴가",
				"#3366FF",
				ScheduleCreatorType.USER,
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
		assertThat(schedule.creatorType()).isEqualTo(ScheduleCreatorType.USER);
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
				null,
				ScheduleCreatorType.AI,
				items
		);

		items.add(createScheduleItem());

		assertThat(schedule.items()).isEmpty();
	}

	@Test
	void acceptsPublicHttpsRepresentativeImageUrl() {
		Schedule schedule = createScheduleWithImage("https://images.example.com/seoul.jpg");

		assertThat(schedule.imageUrl()).isEqualTo("https://images.example.com/seoul.jpg");
	}

	@Test
	void rejectsNonHttpsRepresentativeImageUrl() {
		assertThatThrownBy(() -> createScheduleWithImage("http://localhost/seoul.jpg"))
				.isInstanceOf(InvalidScheduleException.class)
				.hasMessageContaining("public HTTPS URL");
	}

	@Test
	void rejectsPrivateNetworkRepresentativeImageUrl() {
		assertThatThrownBy(() -> createScheduleWithImage("https://192.168.10.10/seoul.jpg"))
				.isInstanceOf(InvalidScheduleException.class)
				.hasMessageContaining("public HTTPS URL");
	}

	@Test
	void enumValuesMatchDatabaseEnumValues() {
		assertThat(ScheduleCreatorType.values()).containsExactly(
				ScheduleCreatorType.USER,
				ScheduleCreatorType.AI,
				ScheduleCreatorType.OTHER
		);
		assertThat(ScheduleItemType.values()).containsExactly(
				ScheduleItemType.MOVE,
				ScheduleItemType.FOOD,
				ScheduleItemType.TOUR,
				ScheduleItemType.STAY,
				ScheduleItemType.ACTIVITY,
				ScheduleItemType.ETC
		);
		assertThat(TripType.values()).containsExactly(
				TripType.ONE_WAY,
				TripType.ROUND_TRIP
		);
		assertThat(FlightDirection.values()).containsExactly(
				FlightDirection.OUTBOUND,
				FlightDirection.RETURN
		);
		assertThat(TransportationType.values()).containsExactly(
				TransportationType.TRAIN_PUBLIC_TRANSIT,
				TransportationType.SHIP_FERRY,
				TransportationType.RENTAL_CAR,
				TransportationType.WALKING,
				TransportationType.OTHER
		);
		assertThat(TravelStyle.values()).containsExactly(
				TravelStyle.TOUR_LANDMARK,
				TravelStyle.RELAXATION_HEALING,
				TravelStyle.FOOD_TOUR,
				TravelStyle.ACTIVITY,
				TravelStyle.OTHER
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

	private Schedule createScheduleWithImage(String imageUrl) {
		return Schedule.create(
				new MemberUuid(UUID.randomUUID()),
				"서울 여행",
				"서울",
				imageUrl,
				LocalDate.of(2026, 8, 10),
				LocalDate.of(2026, 8, 10),
				Headcount.defaultValue(),
				ScheduleCost.zero(),
				null,
				null,
				null,
				null,
				ScheduleCreatorType.AI,
				List.of()
		);
	}
}
