package com.promisetracker.promise;

import com.promisetracker.audit.PromiseActivity;
import com.promisetracker.audit.PromiseActivityAction;
import com.promisetracker.audit.PromiseActivityRepository;
import com.promisetracker.common.exception.ResourceNotFoundException;
import com.promisetracker.common.response.PageResponse;
import com.promisetracker.customer.Customer;
import com.promisetracker.customer.CustomerRepository;
import com.promisetracker.organization.Organization;
import com.promisetracker.organization.OrganizationRepository;
import com.promisetracker.promise.dto.*;
import com.promisetracker.user.User;
import com.promisetracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromiseService {

    private final PromiseRepository promiseRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PromiseActivityRepository activityRepository;

    @Transactional(readOnly = true)
    public PageResponse<PromiseResponse> getPromises(UUID orgId,
                                                      PromiseStatus status,
                                                      PromisePriority priority,
                                                      UUID assignedTo,
                                                      UUID customerId,
                                                      String search,
                                                      LocalDate dueDateFrom,
                                                      LocalDate dueDateTo,
                                                      Pageable pageable) {
        Page<PromiseResponse> page = promiseRepository
                .findByFilters(orgId, status, priority, assignedTo, customerId, search, dueDateFrom, dueDateTo, pageable)
                .map(PromiseResponse::fromEntity);
        return PageResponse.fromPage(page);
    }

    @Transactional(readOnly = true)
    public PromiseResponse getPromiseById(UUID orgId, UUID promiseId) {
        Promise promise = promiseRepository.findByIdAndOrganizationId(promiseId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Promise not found with id: " + promiseId));
        return PromiseResponse.fromEntity(promise);
    }

    @Transactional
    public PromiseResponse createPromise(UUID orgId, UUID currentUserId, CreatePromiseRequest request) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Customer customer = customerRepository.findByIdAndOrganizationId(request.getCustomerId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UUID assigneeId = request.getAssignedTo() != null ? request.getAssignedTo() : currentUserId;
        User assignee = userRepository.findByIdAndOrganizationId(assigneeId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found in organization"));

        Promise promise = Promise.builder()
                .organization(organization)
                .customer(customer)
                .createdBy(creator)
                .assignedTo(assignee)
                .title(request.getTitle())
                .description(request.getDescription())
                .sourceText(request.getSourceText())
                .dueDate(request.getDueDate())
                .priority(request.getPriority())
                .status(PromiseStatus.OPEN)
                .build();

        Promise saved = promiseRepository.save(promise);

        recordActivity(saved, organization, creator, PromiseActivityAction.CREATED, null, saved.getStatus().name());

        return PromiseResponse.fromEntity(saved);
    }

    @Transactional
    public PromiseResponse updatePromise(UUID orgId, UUID currentUserId, UUID promiseId, UpdatePromiseRequest request) {
        Promise promise = promiseRepository.findByIdAndOrganizationId(promiseId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Promise not found with id: " + promiseId));

        Organization organization = promise.getOrganization();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = customerRepository.findByIdAndOrganizationId(request.getCustomerId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        if (request.getAssignedTo() != null && !request.getAssignedTo().equals(promise.getAssignedTo().getId())) {
            User newAssignee = userRepository.findByIdAndOrganizationId(request.getAssignedTo(), orgId)
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found in organization"));
            promise.setAssignedTo(newAssignee);
            recordActivity(promise, organization, currentUser, PromiseActivityAction.ASSIGNED, null, newAssignee.getFirstName() + " " + newAssignee.getLastName());
        }

        String oldStatus = promise.getStatus().name();
        promise.setCustomer(customer);
        promise.setTitle(request.getTitle());
        promise.setDescription(request.getDescription());
        promise.setSourceText(request.getSourceText());
        promise.setDueDate(request.getDueDate());
        promise.setPriority(request.getPriority());

        if (request.getStatus() != null && request.getStatus() != promise.getStatus()) {
            promise.setStatus(request.getStatus());
            if (request.getStatus() == PromiseStatus.COMPLETED) {
                promise.setCompletedAt(Instant.now());
            }
            recordActivity(promise, organization, currentUser, PromiseActivityAction.STATUS_CHANGED, oldStatus, request.getStatus().name());
        } else {
            recordActivity(promise, organization, currentUser, PromiseActivityAction.UPDATED, null, null);
        }

        Promise updated = promiseRepository.save(promise);
        return PromiseResponse.fromEntity(updated);
    }

    @Transactional
    public PromiseResponse updateStatus(UUID orgId, UUID currentUserId, UUID promiseId, PromiseStatus newStatus) {
        Promise promise = promiseRepository.findByIdAndOrganizationId(promiseId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Promise not found with id: " + promiseId));

        String oldStatus = promise.getStatus().name();
        if (promise.getStatus() == newStatus) {
            return PromiseResponse.fromEntity(promise);
        }

        promise.setStatus(newStatus);
        if (newStatus == PromiseStatus.COMPLETED) {
            promise.setCompletedAt(Instant.now());
        } else if (oldStatus.equals("COMPLETED")) {
            promise.setCompletedAt(null);
        }

        Promise saved = promiseRepository.save(promise);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PromiseActivityAction action = switch (newStatus) {
            case COMPLETED -> PromiseActivityAction.COMPLETED;
            case CANCELLED -> PromiseActivityAction.CANCELLED;
            default -> PromiseActivityAction.STATUS_CHANGED;
        };

        recordActivity(saved, saved.getOrganization(), currentUser, action, oldStatus, newStatus.name());

        return PromiseResponse.fromEntity(saved);
    }

    @Transactional
    public PromiseResponse assignPromise(UUID orgId, UUID currentUserId, UUID promiseId, UUID assigneeId) {
        Promise promise = promiseRepository.findByIdAndOrganizationId(promiseId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Promise not found with id: " + promiseId));

        User newAssignee = userRepository.findByIdAndOrganizationId(assigneeId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found in organization"));

        promise.setAssignedTo(newAssignee);
        Promise saved = promiseRepository.save(promise);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        recordActivity(saved, saved.getOrganization(), currentUser, PromiseActivityAction.ASSIGNED, null, newAssignee.getFirstName() + " " + newAssignee.getLastName());

        return PromiseResponse.fromEntity(saved);
    }

    @Transactional
    public void deletePromise(UUID orgId, UUID promiseId) {
        Promise promise = promiseRepository.findByIdAndOrganizationId(promiseId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Promise not found with id: " + promiseId));

        promise.setDeletedAt(Instant.now());
        promiseRepository.save(promise);
    }

    @Transactional(readOnly = true)
    public List<PromiseActivityResponse> getPromiseActivities(UUID orgId, UUID promiseId) {
        promiseRepository.findByIdAndOrganizationId(promiseId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Promise not found with id: " + promiseId));

        return activityRepository.findByPromiseIdAndOrganizationId(promiseId, orgId)
                .stream()
                .map(PromiseActivityResponse::fromEntity)
                .toList();
    }

    private void recordActivity(Promise promise, Organization organization, User user,
                                PromiseActivityAction action, String oldStatus, String newStatus) {
        PromiseActivity activity = PromiseActivity.builder()
                .promise(promise)
                .organization(organization)
                .user(user)
                .action(action)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .build();
        activityRepository.save(activity);
    }
}
