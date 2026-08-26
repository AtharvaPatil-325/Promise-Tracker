package com.promisetracker.dashboard;

import com.promisetracker.common.response.ApiResponse;
import com.promisetracker.dashboard.dto.DashboardSummaryResponse;
import com.promisetracker.promise.dto.PromiseResponse;
import com.promisetracker.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        DashboardSummaryResponse summary = dashboardService.getSummary(orgId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<PromiseResponse>>> getTodayPromises() {
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        List<PromiseResponse> promises = dashboardService.getTodayPromises(orgId);
        return ResponseEntity.ok(ApiResponse.success(promises));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<PromiseResponse>>> getOverduePromises() {
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        List<PromiseResponse> promises = dashboardService.getOverduePromises(orgId);
        return ResponseEntity.ok(ApiResponse.success(promises));
    }
}
