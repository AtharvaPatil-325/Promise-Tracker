package com.promisetracker.analytics;

import com.promisetracker.analytics.dto.AnalyticsOverviewResponse;
import com.promisetracker.analytics.dto.AnalyticsPromisesResponse;
import com.promisetracker.common.response.ApiResponse;
import com.promisetracker.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AnalyticsOverviewResponse>> getOverview() {
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        AnalyticsOverviewResponse overview = analyticsService.getOverview(orgId);
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    @GetMapping("/promises")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AnalyticsPromisesResponse>> getPromiseAnalytics() {
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        AnalyticsPromisesResponse analytics = analyticsService.getPromiseAnalytics(orgId);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}
