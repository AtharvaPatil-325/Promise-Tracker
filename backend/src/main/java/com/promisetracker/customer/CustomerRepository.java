package com.promisetracker.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Query("SELECT c FROM Customer c WHERE c.id = :id AND c.organization.id = :orgId AND c.deletedAt IS NULL")
    Optional<Customer> findByIdAndOrganizationId(@Param("id") UUID id, @Param("orgId") UUID organizationId);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :orgId AND c.deletedAt IS NULL " +
           "AND (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> findByOrganizationIdAndSearch(@Param("orgId") UUID organizationId,
                                                  @Param("search") String search,
                                                  Pageable pageable);
}
