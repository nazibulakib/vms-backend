package com.starkvms.vmsbackend.application.service;

import com.starkvms.vmsbackend.api.dto.AuthSessionResponse;
import com.starkvms.vmsbackend.api.dto.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final AccessTokenService accessTokenService;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                  AccessTokenService accessTokenService) {
        this.authenticationManager = authenticationManager;
        this.accessTokenService = accessTokenService;
    }

    public LoginResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, password));
        AccessTokenService.IssuedToken issuedToken = accessTokenService.issue(authentication.getName());
        return new LoginResponse(issuedToken.value(), "Bearer",
                issuedToken.expiresInSeconds(), authentication.getName());
    }

    public AuthSessionResponse currentSession(String email) {
        return new AuthSessionResponse(email);
    }

    public void logout(String token) {
        accessTokenService.revoke(token);
    }
}
