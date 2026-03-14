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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("null")
class EquipmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void ownerShouldManageEquipmentAndUsersCanViewList() throws Exception {
        String ownerToken = registerAndGetToken("equipment_owner@fitconnect.com", "GYM_OWNER");
        String userToken = registerAndGetToken("equipment_user@fitconnect.com", "GYM_USER");
        Long gymId = createGym(ownerToken, "Equipment Gym");

        Map<String, Object> createPayload = new HashMap<>();
        createPayload.put("gymId", gymId);
        createPayload.put("equipmentName", "Treadmill");
        createPayload.put("quantity", 4);
        createPayload.put("condition", "GOOD");

        String createResponse = mockMvc.perform(post("/api/equipment")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long equipmentId = objectMapper.readTree(createResponse).get("id").asLong();

        Map<String, Object> updatePayload = new HashMap<>(createPayload);
        updatePayload.put("quantity", 5);

        String updateResponse = mockMvc.perform(put("/api/equipment/{id}", equipmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(objectMapper.readTree(updateResponse).get("quantity").asInt()).isEqualTo(5);

        String listResponse = mockMvc.perform(get("/api/equipment/gym/{gymId}", gymId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode equipmentList = objectMapper.readTree(listResponse);
        assertThat(equipmentList).hasSize(1);
        assertThat(equipmentList.get(0).get("equipmentName").asText()).isEqualTo("Treadmill");

        mockMvc.perform(delete("/api/equipment/{id}", equipmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk());

        String afterDelete = mockMvc.perform(get("/api/equipment/gym/{gymId}", gymId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(objectMapper.readTree(afterDelete)).isEmpty();
    }

    private Long createGym(String ownerToken, String gymName) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", gymName);
        payload.put("location", "Bengaluru");
                payload.put("monthlyFee", 2400.0);

        String response = mockMvc.perform(post("/api/gyms")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
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
