package com.fitconnect.service;

import com.fitconnect.dto.PaymentResponse;
import com.fitconnect.entity.Gym;
import com.fitconnect.entity.Payment;
import com.fitconnect.entity.User;
import com.fitconnect.exception.ResourceNotFoundException;
import com.fitconnect.exception.UnauthorizedException;
import com.fitconnect.repository.GymRepository;
import com.fitconnect.repository.PaymentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final GymRepository gymRepository;
    private final CurrentUserService currentUserService;

    public PaymentService(PaymentRepository paymentRepository,
                          GymRepository gymRepository,
                          CurrentUserService currentUserService) {
        this.paymentRepository = paymentRepository;
        this.gymRepository = gymRepository;
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
