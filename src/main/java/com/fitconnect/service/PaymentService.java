package com.fitconnect.service;

import com.fitconnect.dto.PaymentRequest;
import com.fitconnect.dto.PaymentResponse;
import com.fitconnect.entity.Payment;
import com.fitconnect.entity.User;
import com.fitconnect.entity.enums.PaymentStatus;
import com.fitconnect.exception.PaymentException;
import com.fitconnect.exception.ResourceNotFoundException;
import com.fitconnect.exception.UnauthorizedException;
import com.fitconnect.repository.GymRepository;
import com.fitconnect.repository.MembershipRepository;
import com.fitconnect.repository.PaymentRepository;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final GymRepository gymRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipService membershipService;
    private final CurrentUserService currentUserService;

    public PaymentService(PaymentRepository paymentRepository,
                          GymRepository gymRepository,
                          MembershipRepository membershipRepository,
                          MembershipService membershipService,
                          CurrentUserService currentUserService) {
        this.paymentRepository = paymentRepository;
        this.gymRepository = gymRepository;
        this.membershipRepository = membershipRepository;
        this.membershipService = membershipService;
        this.currentUserService = currentUserService;
    }

    public List<PaymentResponse> getMyPayments() {
        User user = currentUserService.getCurrentUser();
        return paymentRepository.findByUser(user).stream().map(this::toResponse).toList();
    }

    public List<PaymentResponse> getGymPayments(Long gymId) {
        User owner = currentUserService.getCurrentUser();
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        if (!gym.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Only the gym owner can view gym payments");
        }

        return paymentRepository.findByGym(gym).stream().map(this::toResponse).toList();
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        User user = currentUserService.getCurrentUser();
        Gym gym = gymRepository.findById(request.getGymId())
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        // Create payment record with PENDING status
        Payment payment = Payment.builder()
                .user(user)
                .gym(gym)
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        // Mock payment processing - simulate 90% success rate
        try {
            mockPaymentVerification();
            payment.setStatus(PaymentStatus.SUCCESS);
        } catch (PaymentException e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw e;
        }

        return toResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse verifyPayment(Long paymentId) {
        User user = currentUserService.getCurrentUser();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Verify user owns this payment
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have access to this payment");
        }

        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        User user = currentUserService.getCurrentUser();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Verify user owns this payment
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have access to this payment");
        }

        // Only allow refunds for successful payments
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new PaymentException("Only successful payments can be refunded");
        }

        // Cancel any ACTIVE memberships for this user at this gym
        membershipService.cancelMembershipsForGym(user, payment.getGym());

        payment.setStatus(PaymentStatus.REFUNDED);
        return toResponse(paymentRepository.save(payment));
    }

    private void mockPaymentVerification() {
        // Mock payment verification - 80% success, 20% failure
        Random random = new Random();
        int result = random.nextInt(100);
        if (result >= 80) {
            throw new PaymentException("Payment verification failed. Please try again.");
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUser().getId())
                .userName(payment.getUser().getName())
                .gymId(payment.getGym().getId())
                .gymName(payment.getGym().getName())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
