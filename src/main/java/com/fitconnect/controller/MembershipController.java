package com.fitconnect.controller;

import com.fitconnect.dto.MembershipPlanOptionResponse;
import com.fitconnect.dto.MembershipRequest;
import com.fitconnect.dto.MembershipResponse;
import com.fitconnect.dto.MembershipStatusUpdateRequest;
import com.fitconnect.service.MembershipService;
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
@RequestMapping("/api/memberships")
@Tag(name = "Memberships", description = "Gym membership purchase and membership management")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Create membership", description = "Allows a gym user to purchase a membership and creates a payment record")
    public ResponseEntity<MembershipResponse> createMembership(@Valid @RequestBody MembershipRequest request) {
        return ResponseEntity.ok(membershipService.createMembership(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Get my memberships", description = "Returns memberships for the authenticated gym user")
    public ResponseEntity<List<MembershipResponse>> getMyMemberships() {
        return ResponseEntity.ok(membershipService.getMyMemberships());
    }

    @GetMapping("/plans")
    @Operation(summary = "Get membership plans", description = "Returns predefined membership plans and pricing multipliers")
    public ResponseEntity<List<MembershipPlanOptionResponse>> getMembershipPlans() {
        return ResponseEntity.ok(membershipService.getMembershipPlans());
    }

    @GetMapping("/gym/{gymId}")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Get gym memberships", description = "Returns memberships for a gym owned by the authenticated owner")
    public ResponseEntity<List<MembershipResponse>> getGymMemberships(@PathVariable Long gymId) {
        return ResponseEntity.ok(membershipService.getGymMemberships(gymId));
    }

    @PutMapping("/{membershipId}/status")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Update membership status", description = "Allows the gym owner to update membership status")
    public ResponseEntity<MembershipResponse> updateMembershipStatus(@PathVariable Long membershipId,
                                                                     @Valid @RequestBody MembershipStatusUpdateRequest request) {
        return ResponseEntity.ok(membershipService.updateMembershipStatus(membershipId, request));
    }

    @PutMapping("/{membershipId}/confirm-payment/{paymentId}")
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Confirm membership payment", description = "Activates membership after successful payment")
    public ResponseEntity<MembershipResponse> confirmMembershipPayment(@PathVariable Long membershipId,
                                                                       @PathVariable Long paymentId) {
        return ResponseEntity.ok(membershipService.confirmMembershipPayment(membershipId, paymentId));
    }
}
