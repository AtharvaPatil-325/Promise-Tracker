package com.promisetracker.analytics;

import com.promisetracker.analytics.dto.AnalyticsOverviewResponse;
import com.promisetracker.analytics.dto.AnalyticsPromisesResponse;
import com.promisetracker.promise.PromisePriority;
import com.promisetracker.promise.PromiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PromiseRepository promiseRepository;

    @Transactional(readOnly = true)
    public AnalyticsOverviewResponse getOverview(UUID orgId) {
        long total = promiseRepository.countByOrganizationId(orgId);
        long completed = promiseRepository.countCompletedByOrganizationId(orgId);
        long overdue = promiseRepository.countOverdueByOrganizationId(orgId, LocalDate.now());
        Double avgDays = promiseRepository.avgCompletionDaysByOrganizationId(orgId);

        double rate = total > 0 ? ((double) completed / total) * 100.0 : 0.0;

        return AnalyticsOverviewResponse.builder()
                .totalPromises(total)
                .completedPromises(completed)
                .overduePromises(overdue)
                .completionRatePercent(Math.round(rate * 10.0) / 10.0)
                .avgCompletionDays(avgDays != null ? Math.round(avgDays * 10.0) / 10.0 : 0.0)
                .build();
    }

    @Transactional(readOnly = true)
    public AnalyticsPromisesResponse getPromiseAnalytics(UUID orgId) {
        // By Priority
        Map<String, Long> priorityMap = new HashMap<>();
        for (PromisePriority p : PromisePriority.values()) {
            priorityMap.put(p.name(), 0L);
        }
        List<Object[]> priorityResults = promiseRepository.countByPriority(orgId);
        for (Object[] row : priorityResults) {
            if (row[0] != null) {
                priorityMap.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }

        // By Assignee
        List<AnalyticsPromisesResponse.AssigneePerformanceDto> assigneeList = new ArrayList<>();
        List<Object[]> assigneeResults = promiseRepository.countByAssignee(orgId);
        for (Object[] row : assigneeResults) {
            UUID userId = (UUID) row[0];
            String firstName = (String) row[1];
            String lastName = (String) row[2];
            long total = ((Number) row[3]).longValue();
            long completed = ((Number) row[4]).longValue();

            assigneeList.add(AnalyticsPromisesResponse.AssigneePerformanceDto.builder()
                    .userId(userId)
                    .userName(firstName + " " + lastName)
                    .totalPromises(total)
                    .completedPromises(completed)
                    .build());
        }

        // By Month
        List<AnalyticsPromisesResponse.MonthlyTrendDto> monthList = new ArrayList<>();
        List<Object[]> monthResults = promiseRepository.countByMonth(orgId);
        for (Object[] row : monthResults) {
            String month = (String) row[0];
            long total = ((Number) row[1]).longValue();
            long completed = ((Number) row[2]).longValue();

            monthList.add(AnalyticsPromisesResponse.MonthlyTrendDto.builder()
                    .month(month)
                    .totalPromises(total)
                    .completedPromises(completed)
                    .build());
        }

        return AnalyticsPromisesResponse.builder()
                .byPriority(priorityMap)
                .byAssignee(assigneeList)
                .byMonth(monthList)
                .build();
    }
}
