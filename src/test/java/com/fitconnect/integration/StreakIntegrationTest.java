package com.fitconnect.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitconnect.entity.Streak;
import com.fitconnect.entity.User;
import com.fitconnect.repository.StreakRepository;
import com.fitconnect.repository.UserRepository;
import java.time.LocalDate;
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
class StreakIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StreakRepository streakRepository;

    @Test
    void workoutAndAttendanceShouldUpdateStreak() throws Exception {
        String ownerToken = registerAndGetToken("streak_owner@fitconnect.com", "GYM_OWNER");
        String userToken = registerAndGetToken("streak_user@fitconnect.com", "GYM_USER");

        Long gymId = createGym(ownerToken);
        logWorkout(userToken);

        User user = userRepository.findByEmail("streak_user@fitconnect.com").orElseThrow();
        Streak streak = streakRepository.findByUser(user).orElseThrow();
        assertThat(streak.getStreakCount()).isEqualTo(1);

        streak.setLastActivityDate(LocalDate.now().minusDays(1));
        streakRepository.save(streak);

        checkInAttendance(userToken, gymId);

        Streak updated = streakRepository.findByUser(user).orElseThrow();
        assertThat(updated.getStreakCount()).isEqualTo(2);
    }

    private void logWorkout(String token) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("exerciseName", "Running");
        payload.put("weight", 70);
        payload.put("reps", 10);
        payload.put("duration", 30);

        mockMvc.perform(post("/api/workouts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    private void checkInAttendance(String token, Long gymId) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("gymId", gymId);

        mockMvc.perform(post("/api/attendance/check-in")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    private Long createGym(String ownerToken) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Streak Gym");
        payload.put("location", "Bengaluru");
        payload.put("monthlyFee", 2100.0);

        String response = mockMvc.perform(post("/api/gyms")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
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
