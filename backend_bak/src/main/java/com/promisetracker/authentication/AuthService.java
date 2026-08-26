package com.promisetracker.authentication;

import com.promisetracker.authentication.dto.AuthResponse;
import com.promisetracker.authentication.dto.LoginRequest;
import com.promisetracker.authentication.dto.RegisterRequest;
import com.promisetracker.authentication.dto.UserResponse;
import com.promisetracker.common.exception.BusinessException;
import com.promisetracker.common.exception.DuplicateResourceException;
import com.promisetracker.organization.Organization;
import com.promisetracker.organization.OrganizationRepository;
import com.promisetracker.security.JwtService;
import com.promisetracker.user.User;
import com.promisetracker.user.UserRepository;
import com.promisetracker.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern PASSWORD_LETTER = Pattern.compile("[A-Za-z]");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile("\\d");

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResult register(RegisterRequest request) {
        validatePasswordStrength(request.getPassword());

        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered");
        }

        Organization organization = organizationRepository.save(Organization.builder()
                .name(request.getOrganizationName().trim())
                .build());

        User user = userRepository.save(User.builder()
                .organization(organization)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .role(UserRole.OWNER)
                .enabled(true)
                .build());

        return issueTokens(user);
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public AuthResult refresh(String refreshToken) {
        RefreshToken stored = refreshTokenService.validate(refreshToken);
        refreshTokenService.revoke(stored);
        return issueTokens(stored.getUser());
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    private AuthResult issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .user(toUserResponse(user))
                .build();
        return new AuthResult(response, refreshToken);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .organizationId(user.getOrganization().getId())
                .organizationName(user.getOrganization().getName())
                .build();
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessException("WEAK_PASSWORD", "Password must be at least 8 characters");
        }
        if (!PASSWORD_LETTER.matcher(password).find() || !PASSWORD_DIGIT.matcher(password).find()) {
            throw new BusinessException("WEAK_PASSWORD",
                    "Password must contain at least one letter and one number");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    public record AuthResult(AuthResponse response, String refreshToken) {
    }
}
