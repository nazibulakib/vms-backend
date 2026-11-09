package com.starkvms.vmsbackend;

import org.springframework.test.context.DynamicPropertyRegistry;

import java.util.UUID;

public final class TestAuthSupport {

    public static final String USERNAME = "vms.test@starkgroup.local";
    public static final String PASSWORD = UUID.randomUUID().toString();

    private TestAuthSupport() {
    }

    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("app.auth.username", () -> USERNAME);
        registry.add("app.auth.password", () -> PASSWORD);
    }
}
