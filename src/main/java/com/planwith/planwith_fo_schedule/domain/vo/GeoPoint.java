package com.planwith.planwith_fo_schedule.domain.vo;

import java.math.BigDecimal;
import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;

public record GeoPoint(BigDecimal latitude, BigDecimal longitude) {

	private static final BigDecimal MIN_LATITUDE = BigDecimal.valueOf(-90);
	private static final BigDecimal MAX_LATITUDE = BigDecimal.valueOf(90);
	private static final BigDecimal MIN_LONGITUDE = BigDecimal.valueOf(-180);
	private static final BigDecimal MAX_LONGITUDE = BigDecimal.valueOf(180);

	public GeoPoint {
		if (latitude == null || longitude == null) {
			throw new InvalidScheduleException("Latitude and longitude must be provided together.");
		}
		if (latitude != null
				&& (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0)) {
			throw new InvalidScheduleException("Latitude must be between -90 and 90.");
		}
		if (longitude != null
				&& (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0)) {
			throw new InvalidScheduleException("Longitude must be between -180 and 180.");
		}
	}
}
