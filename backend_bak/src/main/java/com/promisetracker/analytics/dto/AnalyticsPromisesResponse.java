package com.promisetracker.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class AnalyticsPromisesResponse {

    private Map<String, Long> byPriority;
    private List<AssigneePerformanceDto> byAssignee;
    private List<MonthlyTrendDto> byMonth;

    @Data
    @Builder
    public static class AssigneePerformanceDto {
        private UUID userId;
        private String userName;
        private long totalPromises;
        private long completedPromises;
    }

    @Data
    @Builder
    public static class MonthlyTrendDto {
        private String month;
        private long totalPromises;
        private long completedPromises;
    }
}
