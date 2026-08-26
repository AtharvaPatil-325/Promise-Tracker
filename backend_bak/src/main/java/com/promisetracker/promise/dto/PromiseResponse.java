package com.promisetracker.promise.dto;

import com.promisetracker.promise.Promise;
import com.promisetracker.promise.PromisePriority;
import com.promisetracker.promise.PromiseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PromiseResponse {

    private UUID id;
    private UUID organizationId;
    private UUID customerId;
    private String customerName;
    private String customerCompanyName;
    private UUID createdById;
    private String createdByName;
    private UUID assignedToId;
    private String assignedToName;
    private String title;
    private String description;
    private String sourceText;
    private LocalDate dueDate;
    private PromisePriority priority;
    private PromiseStatus status;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean overdue;

    public static PromiseResponse fromEntity(Promise promise) {
        boolean isOverdue = promise.getDueDate().isBefore(LocalDate.now()) && 
                            promise.getStatus() != PromiseStatus.COMPLETED && 
                            promise.getStatus() != PromiseStatus.CANCELLED;

        return PromiseResponse.builder()
                .id(promise.getId())
                .organizationId(promise.getOrganization().getId())
                .customerId(promise.getCustomer().getId())
                .customerName(promise.getCustomer().getName())
                .customerCompanyName(promise.getCustomer().getCompanyName())
                .createdById(promise.getCreatedBy().getId())
                .createdByName(promise.getCreatedBy().getFirstName() + " " + promise.getCreatedBy().getLastName())
                .assignedToId(promise.getAssignedTo().getId())
                .assignedToName(promise.getAssignedTo().getFirstName() + " " + promise.getAssignedTo().getLastName())
                .title(promise.getTitle())
                .description(promise.getDescription())
                .sourceText(promise.getSourceText())
                .dueDate(promise.getDueDate())
                .priority(promise.getPriority())
                .status(promise.getStatus())
                .completedAt(promise.getCompletedAt())
                .createdAt(promise.getCreatedAt())
                .updatedAt(promise.getUpdatedAt())
                .overdue(isOverdue)
                .build();
    }
}
