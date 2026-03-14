package com.fitconnect.controller;

import com.fitconnect.dto.PaymentResponse;
import com.fitconnect.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment history endpoints for members and gym owners")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Get my payments", description = "Returns payment history for the authenticated gym user")
    public ResponseEntity<List<PaymentResponse>> getMyPayments() {
        return ResponseEntity.ok(paymentService.getMyPayments());
    }

    @GetMapping("/gym/{gymId}")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Get gym payments", description = "Returns payment history for a gym owned by the authenticated owner")
    public ResponseEntity<List<PaymentResponse>> getGymPayments(@PathVariable Long gymId) {
        return ResponseEntity.ok(paymentService.getGymPayments(gymId));
    }
}
