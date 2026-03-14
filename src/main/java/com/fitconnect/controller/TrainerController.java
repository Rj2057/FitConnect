package com.fitconnect.controller;

import com.fitconnect.dto.TrainerProfileUpdateRequest;
import com.fitconnect.dto.TrainerResponse;
import com.fitconnect.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainers", description = "Trainer discovery and trainer profile management")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @GetMapping
    @Operation(summary = "List trainers", description = "Returns all trainer profiles")
    public ResponseEntity<List<TrainerResponse>> getAllTrainers() {
        return ResponseEntity.ok(trainerService.getAllTrainers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trainer by id", description = "Returns a trainer profile by id")
    public ResponseEntity<TrainerResponse> getTrainerById(@PathVariable Long id) {
        return ResponseEntity.ok(trainerService.getTrainerById(id));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('GYM_TRAINER')")
    @Operation(summary = "Update trainer profile", description = "Allows an authenticated trainer to create or update their trainer profile")
    public ResponseEntity<TrainerResponse> updateProfile(@Valid @RequestBody TrainerProfileUpdateRequest request) {
        return ResponseEntity.ok(trainerService.updateProfile(request));
    }
}
