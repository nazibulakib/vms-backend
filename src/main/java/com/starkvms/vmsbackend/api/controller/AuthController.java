package com.starkvms.vmsbackend.api.controller;

import com.starkvms.vmsbackend.api.dto.AuthSessionResponse;
import com.starkvms.vmsbackend.api.dto.LoginRequest;
import com.starkvms.vmsbackend.api.dto.LoginResponse;
import com.starkvms.vmsbackend.application.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request.email().trim(), request.password());
    }

    @GetMapping("/session")
    public AuthSessionResponse currentSession(Authentication authentication) {
        return authenticationService.currentSession(authentication.getName());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.substring("Bearer ".length()).trim();
        authenticationService.logout(token);
        return ResponseEntity.noContent().build();
    }
}
