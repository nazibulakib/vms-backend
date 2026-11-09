package com.starkvms.vmsbackend.api.controller;

import com.jayway.jsonpath.JsonPath;
import com.starkvms.vmsbackend.TestAuthSupport;
import com.starkvms.vmsbackend.infrastructure.persistence.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class VehicleControllerIntegrationTest {

    private static final String API = "/api/v1";

    @DynamicPropertySource
    static void authProperties(DynamicPropertyRegistry registry) {
        TestAuthSupport.registerProperties(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

    private String accessToken;

    @BeforeEach
    void clearVehiclesAndAuthenticate() throws Exception {
        vehicleRepository.deleteAll();
        MvcResult login = mockMvc.perform(post(API + "/auth/login").contextPath(API)
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + TestAuthSupport.USERNAME
                                + "\",\"password\":\"" + TestAuthSupport.PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        accessToken = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
    }

    @Test
    void supportsCreateListReadUpdateAndDelete() throws Exception {
        MvcResult createResult = mockMvc.perform(withAuth(post(API + "/vehicles").contextPath(API)
                        .contentType(APPLICATION_JSON)
                        .content(vehicleJson("abc-123", "Stark Group"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.vehicleNumber").value("ABC-123"))
                .andExpect(jsonPath("$.company").value("Stark Group"))
                .andReturn();
        Long id = ((Number) JsonPath.read(createResult.getResponse().getContentAsString(), "$.id"))
                .longValue();

        mockMvc.perform(withAuth(get(API + "/vehicles").contextPath(API)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].vehicleNumber").value("ABC-123"));

        mockMvc.perform(withAuth(get(API + "/vehicles/{id}", id).contextPath(API)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.model").value("FH"));

        mockMvc.perform(withAuth(put(API + "/vehicles/{id}", id).contextPath(API)
                        .contentType(APPLICATION_JSON)
                        .content(vehicleJson("def-456", "Stark Transport"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleNumber").value("DEF-456"))
                .andExpect(jsonPath("$.company").value("Stark Transport"));

        mockMvc.perform(withAuth(delete(API + "/vehicles/{id}", id).contextPath(API)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(withAuth(get(API + "/vehicles/{id}", id).contextPath(API)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vehicle with id " + id + " was not found."));
    }

    @Test
    void rejectsDuplicateVehicleNumberWithConflict() throws Exception {
        mockMvc.perform(withAuth(post(API + "/vehicles").contextPath(API)
                        .contentType(APPLICATION_JSON)
                        .content(vehicleJson("ABC-123", "Stark Group"))))
                .andExpect(status().isCreated());

        mockMvc.perform(withAuth(post(API + "/vehicles").contextPath(API)
                        .contentType(APPLICATION_JSON)
                        .content(vehicleJson(" abc-123 ", "Different Company"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("A vehicle with this vehicle number already exists."));
    }

    @Test
    void reportsBeanValidationErrorsAsBadRequest() throws Exception {
        mockMvc.perform(withAuth(post(API + "/vehicles").contextPath(API)
                        .contentType(APPLICATION_JSON)
                        .content("{\"vehicleNumber\":\" \",\"company\":\"Stark Group\",\"type\":\"Truck\",\"brand\":\"Volvo\",\"model\":\"FH\",\"status\":\"Active\"}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Vehicle number is required"));
    }

    @Test
    void rejectsVehicleAccessWithoutBearerToken() throws Exception {
        mockMvc.perform(get(API + "/vehicles").contextPath(API))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    private MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    private String vehicleJson(String vehicleNumber, String company) {
        return "{\"vehicleNumber\":\"%s\",\"company\":\"%s\",\"type\":\"Truck\",\"brand\":\"Volvo\",\"model\":\"FH\",\"status\":\"Active\"}"
                .formatted(vehicleNumber, company);
    }
}
