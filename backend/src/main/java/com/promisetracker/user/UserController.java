package com.promisetracker.user;

import com.promisetracker.authentication.dto.UserResponse;
import com.promisetracker.common.response.ApiResponse;
import com.promisetracker.security.SecurityUtils;
import com.promisetracker.user.dto.UpdateUserRoleRequest;
import com.promisetracker.user.dto.UpdateUserStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        List<UserResponse> users = userService.getUsers(orgId);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID id) {
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        UserResponse user = userService.getUserById(orgId, id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        UserResponse user = userService.updateUserRole(orgId, currentUserId, id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User role updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request) {

        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        UserResponse user = userService.updateUserStatus(orgId, currentUserId, id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User status updated successfully"));
    }
}
