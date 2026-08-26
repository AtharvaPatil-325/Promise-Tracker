package com.promisetracker.security;

import com.promisetracker.config.JwtProperties;
import com.promisetracker.user.User;
import com.promisetracker.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(UUID userId, UUID organizationId, UserRole role) {
        return buildToken(userId, organizationId, role, jwtProperties.getAccessTokenExpirationMs());
    }

    public String generateAccessToken(User user) {
        return generateAccessToken(user.getId(), user.getOrganization().getId(), user.getRole());
    }

    public String generateRefreshToken(UUID userId, UUID organizationId, UserRole role) {
        return buildToken(userId, organizationId, role, jwtProperties.getRefreshTokenExpirationMs());
    }

    public String generateRefreshToken(User user) {
        return generateRefreshToken(user.getId(), user.getOrganization().getId(), user.getRole());
    }

    private String buildToken(UUID userId, UUID organizationId, UserRole role, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("org", organizationId.toString())
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims validateAccessToken(String token) {
        return validateToken(token);
    }

    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public UUID extractOrganizationId(Claims claims) {
        return UUID.fromString(claims.get("org", String.class));
    }

    public UserRole extractRole(Claims claims) {
        return UserRole.valueOf(claims.get("role", String.class));
    }

    public UUID getUserIdFromToken(String token) {
        return UUID.fromString(validateToken(token).getSubject());
    }

    public UUID getOrganizationIdFromToken(String token) {
        return UUID.fromString(validateToken(token).get("org", String.class));
    }

    public UserRole getRoleFromToken(String token) {
        return UserRole.valueOf(validateToken(token).get("role", String.class));
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
