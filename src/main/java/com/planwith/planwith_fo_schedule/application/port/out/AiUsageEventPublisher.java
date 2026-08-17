package com.planwith.planwith_fo_schedule.application.port.out;

import java.util.concurrent.CompletableFuture;

import com.planwith.planwith_fo_schedule.application.model.AiUsageReportEvent;

public interface AiUsageEventPublisher {

	CompletableFuture<Void> publish(AiUsageReportEvent event);
}
