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
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userBookingShouldBeVisibleToTrainerAndUser() throws Exception {
        String ownerToken = registerAndGetToken("booking_owner@fitconnect.com", "GYM_OWNER");
        String trainerToken = registerAndGetToken("booking_trainer@fitconnect.com", "GYM_TRAINER");
        String userToken = registerAndGetToken("booking_user@fitconnect.com", "GYM_USER");

        Long gymId = createGym(ownerToken, "Booking Gym");
        Long trainerId = updateTrainerProfileAndGetId(trainerToken, gymId);

        Map<String, Object> bookingPayload = new HashMap<>();
        bookingPayload.put("trainerId", trainerId);
        bookingPayload.put("date", "2026-03-20");
        bookingPayload.put("timeSlot", "07:00-08:00");

        String bookingResponse = mockMvc.perform(post("/api/bookings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingPayload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode bookingJson = objectMapper.readTree(bookingResponse);
        assertThat(bookingJson.get("status").asText()).isEqualTo("PENDING");
        assertThat(bookingJson.get("trainerId").asLong()).isEqualTo(trainerId);

        String userBookings = mockMvc.perform(get("/api/bookings/user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String trainerBookings = mockMvc.perform(get("/api/bookings/trainer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + trainerToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(objectMapper.readTree(userBookings)).hasSize(1);
        assertThat(objectMapper.readTree(trainerBookings)).hasSize(1);
    }

    private Long updateTrainerProfileAndGetId(String token, Long gymId) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("gymId", gymId);
        payload.put("experience", 5);
        payload.put("specialization", "HIIT");

        String response = mockMvc.perform(put("/api/trainers/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createGym(String ownerToken, String gymName) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", gymName);
        payload.put("location", "Bengaluru");
                payload.put("monthlyFee", 2200.0);

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
