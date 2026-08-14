package com.planwith.planwith_fo_schedule.application.port.out;

import java.util.Optional;

public interface DestinationImageSearchPort {

	Optional<String> searchRepresentativeImage(String destination);
}
