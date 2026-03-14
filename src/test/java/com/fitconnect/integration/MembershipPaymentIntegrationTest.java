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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("null")
class MembershipPaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void membershipPurchaseShouldCreatePaymentAndAllowOwnerStatusUpdate() throws Exception {
        String ownerToken = registerAndGetToken("membership_owner@fitconnect.com", "GYM_OWNER");
        String userToken = registerAndGetToken("membership_user@fitconnect.com", "GYM_USER");
        Long gymId = createGym(ownerToken, "Membership Gym");

        Map<String, Object> membershipPayload = new HashMap<>();
        membershipPayload.put("gymId", gymId);
        membershipPayload.put("planName", "ELITE");
        membershipPayload.put("durationMonths", 3);

        String createMembershipResponse = mockMvc.perform(post("/api/memberships")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(membershipPayload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode membershipJson = objectMapper.readTree(createMembershipResponse);
        Long membershipId = membershipJson.get("id").asLong();
        assertThat(membershipJson.get("status").asText()).isEqualTo("ACTIVE");

        String myMembershipsResponse = mockMvc.perform(get("/api/memberships/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(objectMapper.readTree(myMembershipsResponse)).hasSize(1);

        String myPaymentsResponse = mockMvc.perform(get("/api/payments/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode myPayments = objectMapper.readTree(myPaymentsResponse);
        assertThat(myPayments).hasSize(1);
        assertThat(myPayments.get(0).get("status").asText()).isEqualTo("SUCCESS");

        String gymMembershipsResponse = mockMvc.perform(get("/api/memberships/gym/{gymId}", gymId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(objectMapper.readTree(gymMembershipsResponse)).hasSize(1);

        String gymPaymentsResponse = mockMvc.perform(get("/api/payments/gym/{gymId}", gymId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(objectMapper.readTree(gymPaymentsResponse)).hasSize(1);

        Map<String, Object> statusPayload = new HashMap<>();
        statusPayload.put("status", "EXPIRED");

        String updateStatusResponse = mockMvc.perform(put("/api/memberships/{membershipId}/status", membershipId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusPayload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(objectMapper.readTree(updateStatusResponse).get("status").asText()).isEqualTo("EXPIRED");
    }

    private Long createGym(String ownerToken, String gymName) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", gymName);
        payload.put("location", "Bengaluru");
                payload.put("monthlyFee", 2500.0);

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
