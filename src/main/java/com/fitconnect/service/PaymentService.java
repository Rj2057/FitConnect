package com.fitconnect.service;

import com.fitconnect.dto.PaymentRequest;
import com.fitconnect.dto.PaymentResponse;
import com.fitconnect.dto.MonthlyRevenueResponse;
import com.fitconnect.entity.EquipmentRentalTransaction;
import com.fitconnect.entity.Gym;
import com.fitconnect.entity.Membership;
import com.fitconnect.entity.Payment;
import com.fitconnect.entity.User;
import com.fitconnect.entity.enums.MembershipStatus;
import com.fitconnect.entity.enums.PaymentStatus;
import com.fitconnect.exception.PaymentException;
import com.fitconnect.exception.ResourceNotFoundException;
import com.fitconnect.exception.UnauthorizedException;
import com.fitconnect.repository.GymRepository;
import com.fitconnect.repository.EquipmentRentalTransactionRepository;
import com.fitconnect.repository.MembershipRepository;
import com.fitconnect.repository.PaymentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class PaymentService {

    private static final long REFUND_WINDOW_HOURS = 1;

    private final PaymentRepository paymentRepository;
    private final GymRepository gymRepository;
    private final EquipmentRentalTransactionRepository rentalTransactionRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipService membershipService;
    private final CurrentUserService currentUserService;

    public PaymentService(PaymentRepository paymentRepository,
                          GymRepository gymRepository,
                          EquipmentRentalTransactionRepository rentalTransactionRepository,
                          MembershipRepository membershipRepository,
                          MembershipService membershipService,
                          CurrentUserService currentUserService) {
        this.paymentRepository = paymentRepository;
        this.gymRepository = gymRepository;
        this.rentalTransactionRepository = rentalTransactionRepository;
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

    public List<MonthlyRevenueResponse> getGymMonthlyRevenue(Long gymId, int months) {
        User owner = currentUserService.getCurrentUser();
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        if (!gym.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Only the gym owner can view monthly revenue");
        }

        List<Payment> gymPayments = paymentRepository.findByGym(gym);
        List<EquipmentRentalTransaction> marketplaceSales = rentalTransactionRepository.findBySellerGym(gym);
        List<MonthlyRevenueResponse> result = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 0; i < Math.max(1, months); i++) {
            YearMonth target = current.minusMonths(i);
            BigDecimal membershipRevenue = gymPayments.stream()
                    .filter(payment -> payment.getStatus() == PaymentStatus.SUCCESS)
                    .filter(payment -> YearMonth.from(payment.getPaidAt()).equals(target))
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal marketplaceRevenue = marketplaceSales.stream()
                    .filter(tx -> YearMonth.from(tx.getPurchasedAt()).equals(target))
                    .map(EquipmentRentalTransaction::getMonthlyRentPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(MonthlyRevenueResponse.builder()
                    .year(target.getYear())
                    .month(target.getMonthValue())
                    .label(target.getMonth().name() + " " + target.getYear())
                    .membershipRevenue(membershipRevenue)
                    .marketplaceRevenue(marketplaceRevenue)
                    .totalRevenue(membershipRevenue.add(marketplaceRevenue))
                    .build());
        }
        return result;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        User user = currentUserService.getCurrentUser();
        Gym gym = gymRepository.findById(request.getGymId())
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        validateNoActiveMembershipPayment(user, gym);

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

        LocalDateTime refundCutoff = payment.getPaidAt().plusHours(REFUND_WINDOW_HOURS);
        if (LocalDateTime.now().isAfter(refundCutoff)) {
            throw new PaymentException("Refund is only allowed within 1 hour of payment");
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

    private void validateNoActiveMembershipPayment(User user, Gym gym) {
        LocalDate today = LocalDate.now();
        boolean hasActiveMembership = membershipRepository.findByUser(user).stream()
                .anyMatch(membership -> isActiveMembershipForGym(membership, gym.getId(), today));

        if (hasActiveMembership) {
            throw new PaymentException("You already have an active membership for this gym. New payment is allowed after membership ends.");
        }
    }

    private boolean isActiveMembershipForGym(Membership membership, Long gymId, LocalDate today) {
        return membership.getGym().getId().equals(gymId)
                && membership.getStatus() == MembershipStatus.ACTIVE
                && !membership.getEndDate().isBefore(today);
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
