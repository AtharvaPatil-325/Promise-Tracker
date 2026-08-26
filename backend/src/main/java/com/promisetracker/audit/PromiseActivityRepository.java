package com.promisetracker.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PromiseActivityRepository extends JpaRepository<PromiseActivity, UUID> {

    @Query("SELECT a FROM PromiseActivity a WHERE a.promise.id = :promiseId AND a.organization.id = :orgId " +
           "ORDER BY a.createdAt DESC")
    List<PromiseActivity> findByPromiseIdAndOrganizationId(@Param("promiseId") UUID promiseId,
                                                            @Param("orgId") UUID organizationId);
}
