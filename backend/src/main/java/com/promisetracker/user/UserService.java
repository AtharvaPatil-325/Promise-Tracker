package com.promisetracker.user;

import com.promisetracker.authentication.dto.UserResponse;
import com.promisetracker.common.exception.BusinessException;
import com.promisetracker.common.exception.DuplicateResourceException;
import com.promisetracker.common.exception.ResourceNotFoundException;
import com.promisetracker.organization.Organization;
import com.promisetracker.organization.OrganizationRepository;
import com.promisetracker.user.dto.CreateUserRequest;
import com.promisetracker.user.dto.UpdateUserRoleRequest;
import com.promisetracker.user.dto.UpdateUserStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(UUID orgId) {
        return userRepository.findByOrganizationId(orgId)
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID orgId, UUID userId) {
        User user = userRepository.findByIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse createUser(UUID orgId, CreateUserRequest request) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User with email already exists: " + email);
        }

        if (request.getRole() == UserRole.OWNER) {
            throw new BusinessException("Cannot create a secondary OWNER role directly");
        }

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank()) 
                ? request.getPassword() 
                : "Password123!";

        User user = User.builder()
                .organization(organization)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .role(request.getRole())
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    @Transactional
    public UserResponse updateUserRole(UUID orgId, UUID currentUserId, UUID targetUserId, UpdateUserRoleRequest request) {
        User targetUser = userRepository.findByIdAndOrganizationId(targetUserId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));

        if (targetUserId.equals(currentUserId)) {
            throw new BusinessException("Cannot modify your own role");
        }

        if (request.getRole() == UserRole.OWNER) {
            throw new BusinessException("Ownership transfer must be performed via specialized workflow");
        }

        targetUser.setRole(request.getRole());
        User updated = userRepository.save(targetUser);
        return UserResponse.fromEntity(updated);
    }

    @Transactional
    public UserResponse updateUserStatus(UUID orgId, UUID currentUserId, UUID targetUserId, UpdateUserStatusRequest request) {
        User targetUser = userRepository.findByIdAndOrganizationId(targetUserId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));

        if (targetUserId.equals(currentUserId)) {
            throw new BusinessException("Cannot change your own account status");
        }

        targetUser.setEnabled(request.getEnabled());
        User updated = userRepository.save(targetUser);
        return UserResponse.fromEntity(updated);
    }
}
