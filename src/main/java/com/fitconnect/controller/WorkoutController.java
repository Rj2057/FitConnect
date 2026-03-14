package com.fitconnect.controller;

import com.fitconnect.dto.WeeklyCaloriesResponse;
import com.fitconnect.dto.WorkoutRequest;
import com.fitconnect.dto.WorkoutResponse;
import com.fitconnect.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workouts")
@Tag(name = "Workouts", description = "Workout logging and calorie analytics")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Log workout", description = "Creates a workout entry and updates the user's streak")
    public ResponseEntity<WorkoutResponse> logWorkout(@Valid @RequestBody WorkoutRequest request) {
        return ResponseEntity.ok(workoutService.logWorkout(request));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Get workout history", description = "Returns workout history for the authenticated gym user")
    public ResponseEntity<List<WorkoutResponse>> getHistory() {
        return ResponseEntity.ok(workoutService.getWorkoutHistory());
    }

    @GetMapping("/weekly-calories")
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Get weekly calories", description = "Returns total weekly calories burned for the authenticated gym user")
    public ResponseEntity<WeeklyCaloriesResponse> getWeeklyCalories() {
        return ResponseEntity.ok(workoutService.getWeeklyCalories());
    }
}
