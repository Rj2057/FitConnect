package com.fitconnect.service;

import com.fitconnect.dto.MembershipPlanOptionResponse;
import com.fitconnect.dto.MembershipRequest;
import com.fitconnect.dto.MembershipResponse;
import com.fitconnect.dto.MembershipStatusUpdateRequest;
import com.fitconnect.entity.Gym;
import com.fitconnect.entity.Membership;
import com.fitconnect.entity.Payment;
import com.fitconnect.entity.User;
import com.fitconnect.entity.enums.MembershipStatus;
import com.fitconnect.entity.enums.PaymentStatus;
import com.fitconnect.exception.BadRequestException;
import com.fitconnect.exception.ResourceNotFoundException;
import com.fitconnect.exception.UnauthorizedException;
import java.math.BigDecimal;
import com.fitconnect.repository.GymRepository;
import com.fitconnect.repository.MembershipRepository;
import com.fitconnect.repository.PaymentRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class MembershipService {
    private static final String STANDARD_PLAN_NAME = "STANDARD";
    private static final double STANDARD_MULTIPLIER = 1.0;

    private final MembershipRepository membershipRepository;
    private final PaymentRepository paymentRepository;
    private final GymRepository gymRepository;
    private final CurrentUserService currentUserService;

    public MembershipService(MembershipRepository membershipRepository,
                             PaymentRepository paymentRepository,
                             GymRepository gymRepository,
                             CurrentUserService currentUserService) {
        this.membershipRepository = membershipRepository;
        this.paymentRepository = paymentRepository;
        this.gymRepository = gymRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public MembershipResponse createMembership(MembershipRequest request) {
        User user = currentUserService.getCurrentUser();
        Gym gym = gymRepository.findById(request.getGymId())
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        int durationMonths = request.getDurationMonths();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(durationMonths);
        BigDecimal amount = calculateMembershipAmount(gym.getMonthlyFee(), STANDARD_MULTIPLIER, durationMonths);

        boolean hasActiveMembership = membershipRepository.findByUser(user).stream()
                .anyMatch(membership -> membership.getGym().getId().equals(gym.getId())
                        && membership.getStatus() == MembershipStatus.ACTIVE
                        && !membership.getEndDate().isBefore(LocalDate.now()));

        if (hasActiveMembership) {
            throw new BadRequestException("User already has an active membership for this gym");
        }

        Membership membership = Membership.builder()
                .user(user)
                .gym(gym)
                .planName(STANDARD_PLAN_NAME)
                .durationMonths(durationMonths)
                .startDate(startDate)
                .endDate(endDate)
                .status(MembershipStatus.PENDING)
                .build();

        Membership savedMembership = membershipRepository.save(membership);

        return toResponse(savedMembership);
    }

    public List<MembershipPlanOptionResponse> getMembershipPlans() {
        return List.of(
                MembershipPlanOptionResponse.builder()
                        .planName(STANDARD_PLAN_NAME)
                        .description("Single membership plan for all users")
                        .multiplier(STANDARD_MULTIPLIER)
                        .minimumMonths(1)
                        .build()
        );
    }

    public List<MembershipResponse> getMyMemberships() {
        User user = currentUserService.getCurrentUser();
        return membershipRepository.findByUser(user).stream().map(this::toResponse).toList();
    }

    public List<MembershipResponse> getGymMemberships(Long gymId) {
        User owner = currentUserService.getCurrentUser();
        Gym gym = getOwnedGym(gymId, owner.getId());
        return membershipRepository.findByGym(gym).stream().map(this::toResponse).toList();
    }

    @Transactional
    public MembershipResponse updateMembershipStatus(Long membershipId, MembershipStatusUpdateRequest request) {
        User owner = currentUserService.getCurrentUser();
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));

        if (!membership.getGym().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Only the gym owner can update membership status");
        }

        membership.setStatus(request.getStatus());
        return toResponse(membershipRepository.save(membership));
    }

    @Transactional
    public MembershipResponse confirmMembershipPayment(Long membershipId, Long paymentId) {
        User user = currentUserService.getCurrentUser();
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));

        if (!membership.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have access to this membership");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Payment must be successful to confirm membership");
        }

        if (!payment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Payment does not belong to you");
        }

        membership.setStatus(MembershipStatus.ACTIVE);
        return toResponse(membershipRepository.save(membership));
    }

    @Transactional
    public void cancelMembershipsForGym(User user, Gym gym) {
        List<Membership> memberships = membershipRepository.findByUser(user);
        for (Membership membership : memberships) {
            if (membership.getGym().getId().equals(gym.getId()) &&
                membership.getStatus() == MembershipStatus.ACTIVE) {
                membership.setStatus(MembershipStatus.CANCELLED);
                membershipRepository.save(membership);
            }
        }
    }

    private Gym getOwnedGym(Long gymId, Long ownerId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        if (!gym.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You do not own this gym");
        }

        return gym;
    }

    private MembershipResponse toResponse(Membership membership) {
        Payment payment = paymentRepository.findByUser(membership.getUser()).stream()
                .filter(item -> item.getGym().getId().equals(membership.getGym().getId()))
                .max((a, b) -> a.getPaidAt().compareTo(b.getPaidAt()))
                .orElse(null);

        BigDecimal amount = payment != null ? payment.getAmount() 
                : calculateMembershipAmount(membership.getGym().getMonthlyFee(), STANDARD_MULTIPLIER, membership.getDurationMonths());

        return MembershipResponse.builder()
                .id(membership.getId())
                .userId(membership.getUser().getId())
                .userName(membership.getUser().getName())
                .gymId(membership.getGym().getId())
                .gymName(membership.getGym().getName())
                .planName(membership.getPlanName())
                .durationMonths(membership.getDurationMonths())
                .amount(amount)
                .startDate(membership.getStartDate())
                .endDate(membership.getEndDate())
                .status(membership.getStatus())
                .build();
    }

    /**
     * Calculates membership amount with tiered discounts based on duration.
     * Discount structure:
     * - 1-2 months: 0% discount (base price)
     * - 3 months (quarterly): 10% discount
     * - 6 months: 20% discount
     * - 9+ months: 25% discount
     * - 12 months (yearly): 30% discount
     */
    private BigDecimal calculateMembershipAmount(Double monthlyFee, Double planMultiplier, Integer durationMonths) {
        // Base amount without discount
        BigDecimal baseAmount = BigDecimal.valueOf(monthlyFee)
                .multiply(BigDecimal.valueOf(planMultiplier))
                .multiply(BigDecimal.valueOf(durationMonths));
        
        // Determine discount percentage based on duration
        double discountPercentage = getDiscountPercentage(durationMonths);
        
        // Apply discount
        BigDecimal discountMultiplier = BigDecimal.valueOf(1 - (discountPercentage / 100.0));
        return baseAmount.multiply(discountMultiplier).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Returns discount percentage based on membership duration in months.
     */
    private double getDiscountPercentage(Integer durationMonths) {
        if (durationMonths >= 12) {
            return 30.0;  // Yearly: 30% discount
        } else if (durationMonths >= 9) {
            return 25.0;  // 9+ months: 25% discount
        } else if (durationMonths >= 6) {
            return 20.0;  // 6 months: 20% discount
        } else if (durationMonths >= 3) {
            return 10.0;  // Quarterly (3 months): 10% discount
        }
        return 0.0;  // 1-2 months: No discount
    }
}
