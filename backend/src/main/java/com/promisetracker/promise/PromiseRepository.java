package com.promisetracker.promise;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromiseRepository extends JpaRepository<Promise, UUID> {

    @Query("SELECT p FROM Promise p WHERE p.id = :id AND p.organization.id = :orgId AND p.deletedAt IS NULL")
    Optional<Promise> findByIdAndOrganizationId(@Param("id") UUID id, @Param("orgId") UUID organizationId);

    @Query("SELECT p FROM Promise p WHERE p.organization.id = :orgId AND p.deletedAt IS NULL " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:priority IS NULL OR p.priority = :priority) " +
           "AND (:assignedTo IS NULL OR p.assignedTo.id = :assignedTo) " +
           "AND (:customerId IS NULL OR p.customer.id = :customerId) " +
           "AND (CAST(:search AS string) IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "AND (:dueDateFrom IS NULL OR p.dueDate >= :dueDateFrom) " +
           "AND (:dueDateTo IS NULL OR p.dueDate <= :dueDateTo)")
    Page<Promise> findByFilters(@Param("orgId") UUID organizationId,
                                 @Param("status") PromiseStatus status,
                                 @Param("priority") PromisePriority priority,
                                 @Param("assignedTo") UUID assignedTo,
                                 @Param("customerId") UUID customerId,
                                 @Param("search") String search,
                                 @Param("dueDateFrom") LocalDate dueDateFrom,
                                 @Param("dueDateTo") LocalDate dueDateTo,
                                 Pageable pageable);

    @Query("SELECT p FROM Promise p WHERE p.organization.id = :orgId AND p.deletedAt IS NULL " +
           "AND p.dueDate = :date AND p.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Promise> findTodayPromises(@Param("orgId") UUID organizationId, @Param("date") LocalDate date);

    @Query("SELECT p FROM Promise p WHERE p.organization.id = :orgId AND p.deletedAt IS NULL " +
           "AND p.dueDate < :date AND p.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Promise> findOverduePromises(@Param("orgId") UUID organizationId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(p) FROM Promise p WHERE p.organization.id = :orgId AND p.deletedAt IS NULL")
    long countByOrganizationId(@Param("orgId") UUID organizationId);

    @Query("SELECT COUNT(p) FROM Promise p WHERE p.organization.id = :orgId AND p.deletedAt IS NULL " +
           "AND p.status = 'COMPLETED'")
    long countCompletedByOrganizationId(@Param("orgId") UUID organizationId);

    @Query("SELECT COUNT(p) FROM Promise p WHERE p.organization.id = :orgId AND p.deletedAt IS NULL " +
           "AND p.dueDate < :date AND p.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countOverdueByOrganizationId(@Param("orgId") UUID organizationId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(p) FROM Promise p WHERE p.customer.id = :customerId AND p.organization.id = :orgId " +
           "AND p.deletedAt IS NULL AND p.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countOpenByCustomer(@Param("customerId") UUID customerId, @Param("orgId") UUID organizationId);

    @Query("SELECT COUNT(p) FROM Promise p WHERE p.customer.id = :customerId AND p.organization.id = :orgId " +
           "AND p.deletedAt IS NULL AND p.dueDate < :date AND p.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countOverdueByCustomer(@Param("customerId") UUID customerId, @Param("orgId") UUID organizationId,
                                 @Param("date") LocalDate date);

    @Query("SELECT COUNT(p) FROM Promise p WHERE p.customer.id = :customerId AND p.organization.id = :orgId " +
           "AND p.deletedAt IS NULL AND p.status = 'COMPLETED'")
    long countCompletedByCustomer(@Param("customerId") UUID customerId, @Param("orgId") UUID organizationId);

    @Query("SELECT p FROM Promise p WHERE p.customer.id = :customerId AND p.organization.id = :orgId " +
           "AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Promise> findByCustomerId(@Param("customerId") UUID customerId, @Param("orgId") UUID organizationId);

    @Query("SELECT COUNT(p) FROM Promise p WHERE p.organization.id = :orgId AND p.deletedAt IS NULL " +
           "AND p.dueDate = :date AND p.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countTodayByOrganizationId(@Param("orgId") UUID organizationId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(p) FROM Promise p WHERE p.organization.id = :orgId AND p.deletedAt IS NULL " +
           "AND p.status = 'COMPLETED' AND p.completedAt >= :since")
    long countCompletedSince(@Param("orgId") UUID organizationId, @Param("since") java.time.Instant since);

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (completed_at - created_at)) / 86400.0) " +
           "FROM promises WHERE organization_id = :orgId AND status = 'COMPLETED' " +
           "AND completed_at IS NOT NULL AND deleted_at IS NULL", nativeQuery = true)
    Double avgCompletionDaysByOrganizationId(@Param("orgId") UUID organizationId);

    @Query("SELECT p.priority, COUNT(p) FROM Promise p WHERE p.organization.id = :orgId " +
           "AND p.deletedAt IS NULL GROUP BY p.priority")
    List<Object[]> countByPriority(@Param("orgId") UUID organizationId);

    @Query("SELECT p.assignedTo.id, p.assignedTo.firstName, p.assignedTo.lastName, COUNT(p), " +
           "SUM(CASE WHEN p.status = 'COMPLETED' THEN 1 ELSE 0 END) " +
           "FROM Promise p WHERE p.organization.id = :orgId AND p.deletedAt IS NULL " +
           "GROUP BY p.assignedTo.id, p.assignedTo.firstName, p.assignedTo.lastName")
    List<Object[]> countByAssignee(@Param("orgId") UUID organizationId);

    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM') AS month, COUNT(*) AS total, " +
           "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed " +
           "FROM promises WHERE organization_id = :orgId AND deleted_at IS NULL " +
           "GROUP BY TO_CHAR(created_at, 'YYYY-MM') ORDER BY month", nativeQuery = true)
    List<Object[]> countByMonth(@Param("orgId") UUID organizationId);
}
