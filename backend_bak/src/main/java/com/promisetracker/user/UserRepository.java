package com.promisetracker.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByOrganizationId(UUID organizationId);

    Page<User> findByOrganizationId(UUID organizationId, Pageable pageable);

    List<User> findByOrganizationIdAndEnabledTrue(UUID organizationId);
}
