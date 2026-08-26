package com.promisetracker.analytics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyticsOverviewResponse {

    private long totalPromises;
    private long completedPromises;
    private long overduePromises;
    private double completionRatePercent;
    private double avgCompletionDays;
}
