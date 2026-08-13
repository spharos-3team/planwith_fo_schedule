package com.planwith.planwith_fo_schedule.application.port.out;

import java.util.List;
import java.util.Optional;

public interface FlightLocationPort {

	Optional<List<String>> findAirportCodes(String location);
}
