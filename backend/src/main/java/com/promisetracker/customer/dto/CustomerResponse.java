package com.promisetracker.customer.dto;

import com.promisetracker.customer.Customer;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CustomerResponse {

    private UUID id;
    private UUID organizationId;
    private String name;
    private String email;
    private String phone;
    private String companyName;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public static CustomerResponse fromEntity(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .organizationId(customer.getOrganization().getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .companyName(customer.getCompanyName())
                .notes(customer.getNotes())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
