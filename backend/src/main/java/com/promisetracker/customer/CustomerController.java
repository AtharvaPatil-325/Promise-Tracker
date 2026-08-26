package com.promisetracker.customer;

import com.promisetracker.common.response.ApiResponse;
import com.promisetracker.common.response.PageResponse;
import com.promisetracker.customer.dto.CreateCustomerRequest;
import com.promisetracker.customer.dto.CustomerResponse;
import com.promisetracker.customer.dto.UpdateCustomerRequest;
import com.promisetracker.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> getCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        int pageSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("name").ascending());
        
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        PageResponse<CustomerResponse> customers = customerService.getCustomers(orgId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable UUID id) {
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        CustomerResponse customer = customerService.getCustomerById(orgId, id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MEMBER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        CustomerResponse customer = customerService.createCustomer(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(customer, "Customer created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        CustomerResponse customer = customerService.updateCustomer(orgId, id, request);
        return ResponseEntity.ok(ApiResponse.success(customer, "Customer updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable UUID id) {
        UUID orgId = SecurityUtils.getCurrentOrganizationId();
        customerService.deleteCustomer(orgId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Customer deleted successfully"));
    }
}
