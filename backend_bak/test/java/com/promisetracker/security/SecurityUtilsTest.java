package com.promisetracker.security;

import com.promisetracker.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    private UUID userId;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orgId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, orgId, "test@example.com", "hashedpass", UserRole.OWNER, true);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_ReturnsPrincipal() {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        assertNotNull(principal);
        assertEquals(userId, principal.getId());
        assertEquals(orgId, principal.getOrganizationId());
    }

    @Test
    void getCurrentOrganizationId_ReturnsOrgId() {
        assertEquals(orgId, SecurityUtils.getCurrentOrganizationId());
    }
}
