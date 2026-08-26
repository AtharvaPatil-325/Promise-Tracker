package com.promisetracker.customer;

import com.promisetracker.common.exception.ResourceNotFoundException;
import com.promisetracker.customer.dto.CreateCustomerRequest;
import com.promisetracker.customer.dto.CustomerResponse;
import com.promisetracker.organization.Organization;
import com.promisetracker.organization.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private CustomerService customerService;

    private UUID orgId;
    private Organization organization;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        organization = Organization.builder().id(orgId).name("Test Org").build();
    }

    @Test
    void createCustomer_Success() {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
                .name("Rahul Sharma")
        .email("rahul@example.com")
        .phone("9876543210")
        .companyName("Acme Corp")
        .build();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CustomerResponse response = customerService.createCustomer(orgId, request);

        assertNotNull(response);
        assertEquals("Rahul Sharma", response.getName());
        assertEquals("rahul@example.com", response.getEmail());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findByIdAndOrganizationId(customerId, orgId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(orgId, customerId));
    }
}
