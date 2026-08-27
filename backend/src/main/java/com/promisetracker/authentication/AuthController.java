package com.promisetracker.authentication;

import com.promisetracker.authentication.dto.AuthResponse;
import com.promisetracker.authentication.dto.LoginRequest;
import com.promisetracker.authentication.dto.RefreshRequest;
import com.promisetracker.authentication.dto.RegisterRequest;
import com.promisetracker.common.response.ApiResponse;
import com.promisetracker.config.JwtProperties;
import com.promisetracker.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String AUTH_COOKIE_PATH = "/api/v1/auth";

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        AuthService.AuthResult result = authService.register(request);
        addRefreshTokenCookie(httpRequest, response, result.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(result.response(), "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        AuthService.AuthResult result = authService.login(request);
        addRefreshTokenCookie(httpRequest, response, result.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(result.response(), "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String cookieToken,
            @RequestBody(required = false) RefreshRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String refreshToken = resolveRefreshToken(cookieToken, request);
        AuthService.AuthResult result = authService.refresh(refreshToken);
        addRefreshTokenCookie(httpRequest, response, result.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(result.response(), "Token refreshed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        authService.logout(SecurityUtils.getCurrentUserId());
        clearRefreshTokenCookie(httpRequest, response);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    private String resolveRefreshToken(String cookieToken, RefreshRequest request) {
        if (StringUtils.hasText(cookieToken)) {
            return cookieToken;
        }
        if (request != null && StringUtils.hasText(request.getRefreshToken())) {
            return request.getRefreshToken();
        }
        throw new BadCredentialsException("Refresh token is required");
    }

    private void addRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        boolean isSecure = request.isSecure();
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(isSecure)
                .sameSite(isSecure ? "Strict" : "Lax")
                .path(AUTH_COOKIE_PATH)
                .maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response) {
        boolean isSecure = request.isSecure();
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(isSecure)
                .sameSite(isSecure ? "Strict" : "Lax")
                .path(AUTH_COOKIE_PATH)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
