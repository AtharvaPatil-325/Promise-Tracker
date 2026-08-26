package com.promisetracker.authentication;

import com.promisetracker.config.JwtProperties;
import com.promisetracker.security.JwtService;
import com.promisetracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    public String createRefreshToken(User user) {
        String token = jwtService.generateRefreshToken(user);
        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(token))
                .expiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(entity);
        return token;
    }

    @Transactional(readOnly = true)
    public RefreshToken validate(String token) {
        UUID userId = jwtService.getUserIdFromToken(token);

        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(hashToken(token))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (!stored.getUser().getId().equals(userId)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expired");
        }
        return stored;
    }

    @Transactional
    public void revoke(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
