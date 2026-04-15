package com.fitconnect.controller;

import com.fitconnect.dto.PaymentRequest;
import com.fitconnect.dto.PaymentResponse;
import com.fitconnect.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/process")
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Process payment", description = "Processes a payment request with mock verification")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(request));
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Verify payment", description = "Retrieves payment details and status")
    public ResponseEntity<PaymentResponse> verifyPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.verifyPayment(paymentId));
    }

    @PutMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Refund payment", description = "Initiates a refund for a successful payment")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }
}
