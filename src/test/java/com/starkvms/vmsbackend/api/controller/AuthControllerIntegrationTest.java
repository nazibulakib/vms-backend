package com.starkvms.vmsbackend.api.controller;

import com.jayway.jsonpath.JsonPath;
import com.starkvms.vmsbackend.TestAuthSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class AuthControllerIntegrationTest {

    private static final String API = "/api/v1";

    @DynamicPropertySource
    static void authProperties(DynamicPropertyRegistry registry) {
        TestAuthSupport.registerProperties(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginIssuesBearerTokenThatCanAccessVehicleEndpointsAndLogoutRevokesIt() throws Exception {
        MvcResult loginResult = mockMvc.perform(post(API + "/auth/login").contextPath(API)
                        .contentType(APPLICATION_JSON)
                        .content(credentials(TestAuthSupport.USERNAME, TestAuthSupport.PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(28800))
                .andExpect(jsonPath("$.email").value(TestAuthSupport.USERNAME))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();
        String token = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(get(API + "/auth/session").contextPath(API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TestAuthSupport.USERNAME));

        mockMvc.perform(get(API + "/vehicles").contextPath(API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post(API + "/auth/logout").contextPath(API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(API + "/auth/session").contextPath(API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidCredentialsReturnClearUnauthorizedResponse() throws Exception {
        mockMvc.perform(post(API + "/auth/login").contextPath(API)
                        .contentType(APPLICATION_JSON)
                        .content(credentials(TestAuthSupport.USERNAME, "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void vehicleOperationsRejectMissingBearerToken() throws Exception {
        mockMvc.perform(get(API + "/vehicles").contextPath(API))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    private String credentials(String email, String password) {
        return "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
    }
}
