package com.planwith.planwith_fo_schedule.application.port.out;

import java.time.LocalDate;
import java.util.List;

import com.planwith.planwith_fo_schedule.application.model.FlightCandidate;

public interface FlightSearchPort {

	List<FlightCandidate> search(FlightSearchCriteria criteria);

	record FlightSearchCriteria(
			String departureAirportCode,
			String arrivalAirportCode,
			LocalDate flightDate,
			String carrierCode,
			String flightNumber
	) {
		public FlightSearchCriteria(
				String departureAirportCode,
				String arrivalAirportCode,
				LocalDate flightDate
		) {
			this(departureAirportCode, arrivalAirportCode, flightDate, null, null);
		}
	}
}
