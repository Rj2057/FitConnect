package com.fitconnect.controller;

import com.fitconnect.dto.GymDetailsResponse;
import com.fitconnect.dto.GymMemberResponse;
import com.fitconnect.dto.GymRequest;
import com.fitconnect.dto.GymReviewRequest;
import com.fitconnect.dto.GymReviewResponse;
import com.fitconnect.dto.GymResponse;
import com.fitconnect.service.GymService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gyms")
@Tag(name = "Gyms", description = "Gym creation, listing, updates, and members")
public class GymController {

    private final GymService gymService;

    public GymController(GymService gymService) {
        this.gymService = gymService;
    }

    @PostMapping
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Create gym", description = "Allows a gym owner to create a gym")
    public ResponseEntity<GymResponse> createGym(@Valid @RequestBody GymRequest request) {
        return ResponseEntity.ok(gymService.createGym(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Update gym", description = "Allows the owning gym owner to update gym details")
    public ResponseEntity<GymResponse> updateGym(@PathVariable Long id, @Valid @RequestBody GymRequest request) {
        return ResponseEntity.ok(gymService.updateGym(id, request));
    }

    @GetMapping
    @Operation(summary = "List gyms", description = "Returns all available gyms")
    public ResponseEntity<List<GymResponse>> getAllGyms() {
        return ResponseEntity.ok(gymService.getAllGyms());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get gym by id", description = "Returns gym details for a specific gym")
    public ResponseEntity<GymResponse> getGymById(@PathVariable Long id) {
        return ResponseEntity.ok(gymService.getGymById(id));
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "Get gym details", description = "Returns gym details including fee, equipment, rating, and reviews")
    public ResponseEntity<GymDetailsResponse> getGymDetails(@PathVariable Long id) {
        return ResponseEntity.ok(gymService.getGymDetails(id));
    }

    @PostMapping("/{id}/reviews")
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Add or update gym review", description = "Allows a gym user to post one review per gym and update it later")
    public ResponseEntity<GymReviewResponse> addOrUpdateReview(@PathVariable Long id,
                                                               @Valid @RequestBody GymReviewRequest request) {
        return ResponseEntity.ok(gymService.addOrUpdateGymReview(id, request));
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Get gym members", description = "Returns members for a gym owned by the authenticated owner")
    public ResponseEntity<List<GymMemberResponse>> getGymMembers(@PathVariable Long id) {
        return ResponseEntity.ok(gymService.getGymMembers(id));
    }
}
