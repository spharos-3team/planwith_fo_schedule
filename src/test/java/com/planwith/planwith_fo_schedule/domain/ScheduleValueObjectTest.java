package com.planwith.planwith_fo_schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_schedule.domain.vo.GeoPoint;
import com.planwith.planwith_fo_schedule.domain.vo.Headcount;
import com.planwith.planwith_fo_schedule.domain.vo.MemberUuid;
import com.planwith.planwith_fo_schedule.domain.vo.ScheduleCost;
import com.planwith.planwith_fo_schedule.domain.vo.SchedulePeriod;

class ScheduleValueObjectTest {

	@Test
	void wrapsExternalMemberUuid() {
		UUID value = UUID.randomUUID();

		MemberUuid memberUuid = new MemberUuid(value);

		assertThat(memberUuid.value()).isEqualTo(value);
	}

	@Test
	void appliesSqlDefaultValues() {
		assertThat(Headcount.defaultValue().value()).isEqualTo(1);
		assertThat(ScheduleCost.zero().amount()).isZero();
		assertThat(ScheduleCost.unspecified().amount()).isNull();
	}

	@Test
	void rejectsEndDateBeforeStartDate() {
		assertThatThrownBy(() -> new SchedulePeriod(
				LocalDate.of(2026, 8, 11),
				LocalDate.of(2026, 8, 10)
		))
				.isInstanceOf(InvalidScheduleException.class)
				.hasMessage("End date must not be before start date.");
	}

	@Test
	void rejectsCoordinatesOutsideEarthRange() {
		assertThatThrownBy(() -> new GeoPoint(new BigDecimal("90.0000001"), BigDecimal.ZERO))
				.isInstanceOf(InvalidScheduleException.class)
				.hasMessage("Latitude must be between -90 and 90.");
	}
}
