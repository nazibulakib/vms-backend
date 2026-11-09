package com.starkvms.vmsbackend;

import com.starkvms.vmsbackend.application.service.DatabaseConnectivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VmsBackendApplicationTests {

    @DynamicPropertySource
    static void authProperties(DynamicPropertyRegistry registry) {
        TestAuthSupport.registerProperties(registry);
    }

    @Autowired
    private DatabaseConnectivityService databaseConnectivityService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void databaseConnectivityCheckSucceeds() {
        assertTrue(databaseConnectivityService.isAvailable());
    }

    @Test
    void databaseHealthEndpointReportsUp() throws Exception {
        mockMvc.perform(get("/api/v1/health/database").contextPath("/api/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("UP"));
    }
}
