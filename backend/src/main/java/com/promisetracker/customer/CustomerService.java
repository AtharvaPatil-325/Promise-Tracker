package com.promisetracker.customer;

import com.promisetracker.common.exception.ResourceNotFoundException;
import com.promisetracker.common.response.PageResponse;
import com.promisetracker.customer.dto.CreateCustomerRequest;
import com.promisetracker.customer.dto.CustomerResponse;
import com.promisetracker.customer.dto.UpdateCustomerRequest;
import com.promisetracker.organization.Organization;
import com.promisetracker.organization.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> getCustomers(UUID orgId, String search, Pageable pageable) {
        Page<CustomerResponse> page = customerRepository
                .findByOrganizationIdAndSearch(orgId, search, pageable)
                .map(CustomerResponse::fromEntity);
        return PageResponse.fromPage(page);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID orgId, UUID customerId) {
        Customer customer = customerRepository.findByIdAndOrganizationId(customerId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        return CustomerResponse.fromEntity(customer);
    }

    @Transactional
    public CustomerResponse createCustomer(UUID orgId, CreateCustomerRequest request) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + orgId));

        Customer customer = Customer.builder()
                .organization(organization)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .companyName(request.getCompanyName())
                .notes(request.getNotes())
                .build();

        Customer saved = customerRepository.save(customer);
        return CustomerResponse.fromEntity(saved);
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID orgId, UUID customerId, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findByIdAndOrganizationId(customerId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCompanyName(request.getCompanyName());
        customer.setNotes(request.getNotes());

        Customer updated = customerRepository.save(customer);
        return CustomerResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteCustomer(UUID orgId, UUID customerId) {
        Customer customer = customerRepository.findByIdAndOrganizationId(customerId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        customer.setDeletedAt(Instant.now());
        customerRepository.save(customer);
    }
}
