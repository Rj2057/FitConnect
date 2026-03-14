package com.fitconnect.controller;

import com.fitconnect.dto.StreakResponse;
import com.fitconnect.service.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/streak")
@Tag(name = "Streaks", description = "Streak tracking and streak status endpoints")
public class StreakController {

    private final StreakService streakService;

    public StreakController(StreakService streakService) {
        this.streakService = streakService;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Update streak", description = "Updates streak state for the authenticated user")
    public ResponseEntity<StreakResponse> updateStreak() {
        return ResponseEntity.ok(streakService.updateForCurrentUser());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get streak by user id", description = "Returns streak information for a specific user")
    public ResponseEntity<StreakResponse> getStreak(@PathVariable Long userId) {
        return ResponseEntity.ok(streakService.getByUserId(userId));
    }
}
