package com.planwith.planwith_fo_schedule.application.port.out;

import com.planwith.planwith_fo_schedule.application.model.AiUsageReportEvent;

public interface AiUsageReportingPort {

	void report(AiUsageReportEvent event);
}
