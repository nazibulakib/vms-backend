package com.starkvms.vmsbackend.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccessTokenService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, TokenSession> sessions = new ConcurrentHashMap<>();
    private final long tokenTtlSeconds;

    public AccessTokenService(
            @Value("${app.auth.token-ttl-seconds:28800}") long tokenTtlSeconds) {
        if (tokenTtlSeconds <= 0) {
            throw new IllegalArgumentException("Authentication token lifetime must be positive.");
        }
        this.tokenTtlSeconds = tokenTtlSeconds;
    }

    public IssuedToken issue(String email) {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = now.plusSeconds(tokenTtlSeconds);
        sessions.put(digest(token), new TokenSession(email, expiresAt));
        return new IssuedToken(token, tokenTtlSeconds);
    }

    public Authentication authenticate(String token) {
        String tokenDigest = digest(token);
        TokenSession session = sessions.get(tokenDigest);
        if (session == null) {
            return null;
        }
        if (!session.expiresAt().isAfter(Instant.now())) {
            sessions.remove(tokenDigest, session);
            return null;
        }
        return UsernamePasswordAuthenticationToken.authenticated(
                session.email(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            sessions.remove(digest(token));
        }
    }

    private String digest(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    public record IssuedToken(String value, long expiresInSeconds) {
    }

    private record TokenSession(String email, Instant expiresAt) {
    }
}
