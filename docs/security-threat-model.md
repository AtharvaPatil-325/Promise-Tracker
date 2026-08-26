# PromiseTracker Security Threat Model

This document identifies security threats for PromiseTracker and documents mitigations.

## 1. Authentication Threats

| Threat | Description | Mitigation |
|--------|-------------|------------|
| Credential stuffing | Attackers use leaked credentials to log in | Rate limiting on `/login`, strong password policy, generic error messages |
| Brute force | Automated password guessing | Rate limiting (Bucket4j), no user enumeration |
| Weak passwords | Users choose easily guessable passwords | Minimum 8 chars, complexity validation, BCrypt hashing (cost factor 12) |
| Token theft | Stolen JWT/refresh token used by attacker | Short access token TTL (15 min), HttpOnly Secure SameSite cookies for refresh tokens |

## 2. Authorization Threats

| Threat | Description | Mitigation |
|--------|-------------|------------|
| Privilege escalation | MEMBER gains OWNER permissions | RBAC via `@PreAuthorize` on every endpoint |
| Missing authorization | Endpoint accessible without role check | Spring Security method-level security; deny-by-default |

## 3. Tenant Isolation Threats

| Threat | Description | Mitigation |
|--------|-------------|------------|
| Cross-tenant data access (IDOR/BOLA) | User from Org A accesses Org B resources | Every query includes `organization_id` from JWT; never trust client org ID |
| Tenant ID injection | Client sends organizationId in body | Organization ID derived exclusively from JWT claims |

## 4. Injection Attacks

| Threat | Description | Mitigation |
|--------|-------------|------------|
| SQL injection | Malicious input in search/filter params | Parameterized queries via Spring Data JPA |
| XSS | Stored malicious scripts in text fields | React renders strings as text; CSP headers |

## 5. Sensitive Data Exposure

| Threat | Description | Mitigation |
|--------|-------------|------------|
| Password hash exposure | API returns password_hash | DTOs never include password_hash |
| Stack trace in API response | Internal errors expose code paths | Global exception handler returns safe error DTOs |
| Excessive logging | Passwords/tokens logged | Never log Authorization headers or passwords |

## 6. Chrome Extension Threats

| Threat | Description | Mitigation |
|--------|-------------|------------|
| Excessive permissions | Extension reads all page content | Minimal permissions: contextMenus, storage, activeTab |
| Token exposure to webpages | Auth token injected into page DOM | Tokens in extension storage only |
| Full page capture | Entire webpage sent to backend | Only user-selected text; max length enforced |

## 7. Rate Limiting & DoS

| Threat | Description | Mitigation |
|--------|-------------|------------|
| Brute force on auth endpoints | High-volume login attempts | Bucket4j: 10 req/min on auth endpoints |
| Oversized payloads | Large JSON bodies | Request size limits; field length validation |

## 8. CORS Misconfiguration

| Threat | Description | Mitigation |
|--------|-------------|------------|
| Wildcard CORS | allowedOrigins = "*" | Explicit allowed origins from environment config |

## 9. Concurrency & Data Integrity

| Threat | Description | Mitigation |
|--------|-------------|------------|
| Lost updates | Concurrent promise edits | Optimistic locking with @Version |
| Partial writes | Promise created but audit fails | @Transactional on multi-step operations |

## 10. Infrastructure

| Threat | Description | Mitigation |
|--------|-------------|------------|
| HTTP credential transmission | Credentials over HTTP | HTTPS in production; HSTS header |
| Database exposure | PostgreSQL on internet | Docker network isolation |
