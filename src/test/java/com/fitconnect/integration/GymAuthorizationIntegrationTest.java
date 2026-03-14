package com.fitconnect.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("null")
class GymAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void onlyGymOwnerCanCreateGym() throws Exception {
        String ownerToken = registerAndGetToken("owner_test@fitconnect.com", "GYM_OWNER");
        String userToken = registerAndGetToken("normal_test@fitconnect.com", "GYM_USER");

        Map<String, Object> gymPayload = new HashMap<>();
        gymPayload.put("name", "Power House");
        gymPayload.put("location", "Mysuru");
        gymPayload.put("monthlyFee", 1800.0);

        String ownerResponse = mockMvc.perform(post("/api/gyms")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gymPayload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode ownerJson = objectMapper.readTree(ownerResponse);
        assertThat(ownerJson.get("name").asText()).isEqualTo("Power House");

        mockMvc.perform(post("/api/gyms")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gymPayload)))
                .andExpect(status().isForbidden());
    }

    private String registerAndGetToken(String email, String role) throws Exception {
        Map<String, Object> registerPayload = new HashMap<>();
        registerPayload.put("name", "Test " + role);
        registerPayload.put("email", email);
        registerPayload.put("password", "StrongPass@123");
        registerPayload.put("role", role);

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }
}
