package com.promisetracker.promise;

import com.promisetracker.common.response.ApiResponse;
import com.promisetracker.common.response.PageResponse;
import com.promisetracker.promise.dto.*;
import com.promisetracker.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/promises")
@RequiredArgsConstructor
public class PromiseController {

    private final PromiseService promiseService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PromiseResponse>>> getPromises(
            @RequestParam(required = false) PromiseStatus status,
            @RequestParam(required = false) PromisePriority priority,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        int pageSize = Math.min(Math.max(size, 1), 100);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        PageResponse<PromiseResponse> promises = promiseService.getPromises(
                orgId, status, priority, assignedTo, customerId, search, dueDateFrom, dueDateTo, pageable);
        return ResponseEntity.ok(ApiResponse.success(promises));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromiseResponse>> getPromise(@PathVariable UUID id) {
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        PromiseResponse promise = promiseService.getPromiseById(orgId, id);
        return ResponseEntity.ok(ApiResponse.success(promise));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MEMBER')")
    public ResponseEntity<ApiResponse<PromiseResponse>> createPromise(
            @Valid @RequestBody CreatePromiseRequest request) {

        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        UUID userId = SecurityUtils.getCurrentUserId();
        PromiseResponse promise = promiseService.createPromise(orgId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(promise, "Promise created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MEMBER')")
    public ResponseEntity<ApiResponse<PromiseResponse>> updatePromise(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePromiseRequest request) {

        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        UUID userId = SecurityUtils.getCurrentUserId();
        PromiseResponse promise = promiseService.updatePromise(orgId, userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(promise, "Promise updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MEMBER')")
    public ResponseEntity<ApiResponse<PromiseResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePromiseStatusRequest request) {

        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        UUID userId = SecurityUtils.getCurrentUserId();
        PromiseResponse promise = promiseService.updateStatus(orgId, userId, id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(promise, "Promise status updated successfully"));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PromiseResponse>> assignPromise(
            @PathVariable UUID id,
            @Valid @RequestBody AssignPromiseRequest request) {

        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        UUID userId = SecurityUtils.getCurrentUserId();
        PromiseResponse promise = promiseService.assignPromise(orgId, userId, id, request.getAssignedTo());
        return ResponseEntity.ok(ApiResponse.success(promise, "Promise assigned successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePromise(@PathVariable UUID id) {
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        promiseService.deletePromise(orgId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Promise deleted successfully"));
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<List<PromiseActivityResponse>>> getPromiseActivities(@PathVariable UUID id) {
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        List<PromiseActivityResponse> activities = promiseService.getPromiseActivities(orgId, id);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
}
