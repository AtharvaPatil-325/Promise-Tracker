package com.promisetracker.dashboard;

import com.promisetracker.dashboard.dto.DashboardSummaryResponse;
import com.promisetracker.promise.PromiseRepository;
import com.promisetracker.promise.dto.PromiseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PromiseRepository promiseRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(UUID orgId) {
        LocalDate today = LocalDate.now();
        Instant startOfWeek = today.with(DayOfWeek.MONDAY).atStartOfDay(ZoneOffset.UTC).toInstant();

        long todayCount = promiseRepository.countTodayByOrganizationId(orgId, today);
        long overdueCount = promiseRepository.countOverdueByOrganizationId(orgId, today);
        long completedThisWeekCount = promiseRepository.countCompletedSince(orgId, startOfWeek);
        long totalPromises = promiseRepository.countByOrganizationId(orgId);
        long completedPromises = promiseRepository.countCompletedByOrganizationId(orgId);

        double completionRate = totalPromises > 0 ? ((double) completedPromises / totalPromises) * 100.0 : 0.0;

        return DashboardSummaryResponse.builder()
                .todayCount(todayCount)
                .overdueCount(overdueCount)
                .completedThisWeekCount(completedThisWeekCount)
                .completionRatePercent(Math.round(completionRate * 10.0) / 10.0)
                .build();
    }

    @Transactional(readOnly = true)
    public List<PromiseResponse> getTodayPromises(UUID orgId) {
        return promiseRepository.findTodayPromises(orgId, LocalDate.now())
                .stream()
                .map(PromiseResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PromiseResponse> getOverduePromises(UUID orgId) {
        return promiseRepository.findOverduePromises(orgId, LocalDate.now())
                .stream()
                .map(PromiseResponse::fromEntity)
                .toList();
    }
}
