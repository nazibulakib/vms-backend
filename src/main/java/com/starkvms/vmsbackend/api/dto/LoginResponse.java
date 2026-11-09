package com.starkvms.vmsbackend.api.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String email) {
}
