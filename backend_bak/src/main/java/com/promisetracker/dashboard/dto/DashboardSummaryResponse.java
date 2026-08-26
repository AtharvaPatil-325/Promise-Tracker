package com.promisetracker.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryResponse {

    private long todayCount;
    private long overdueCount;
    private long completedThisWeekCount;
    private double completionRatePercent;
}
