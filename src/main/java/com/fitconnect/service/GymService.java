package com.fitconnect.service;

import com.fitconnect.dto.EquipmentResponse;
import com.fitconnect.dto.GymDetailsResponse;
import com.fitconnect.dto.GymMemberResponse;
import com.fitconnect.dto.GymRequest;
import com.fitconnect.dto.GymReviewRequest;
import com.fitconnect.dto.GymReviewResponse;
import com.fitconnect.dto.GymResponse;
import com.fitconnect.entity.Equipment;
import com.fitconnect.entity.Gym;
import com.fitconnect.entity.GymReview;
import com.fitconnect.entity.Membership;
import com.fitconnect.entity.User;
import com.fitconnect.entity.enums.Role;
import com.fitconnect.exception.ResourceNotFoundException;
import com.fitconnect.exception.UnauthorizedException;
import com.fitconnect.repository.EquipmentRepository;
import com.fitconnect.repository.GymRepository;
import com.fitconnect.repository.GymReviewRepository;
import com.fitconnect.repository.MembershipRepository;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class GymService {

    private final GymRepository gymRepository;
    private final MembershipRepository membershipRepository;
    private final EquipmentRepository equipmentRepository;
    private final GymReviewRepository gymReviewRepository;
    private final CurrentUserService currentUserService;

    public GymService(GymRepository gymRepository,
                      MembershipRepository membershipRepository,
                      EquipmentRepository equipmentRepository,
                      GymReviewRepository gymReviewRepository,
                      CurrentUserService currentUserService) {
        this.gymRepository = gymRepository;
        this.membershipRepository = membershipRepository;
        this.equipmentRepository = equipmentRepository;
        this.gymReviewRepository = gymReviewRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public GymResponse createGym(GymRequest request) {
        User owner = currentUserService.getCurrentUser();
        if (owner.getRole() != Role.GYM_OWNER) {
            throw new UnauthorizedException("Only gym owners can create gyms");
        }

        Gym gym = Gym.builder()
                .name(request.getName())
                .location(request.getLocation())
            .monthlyFee(request.getMonthlyFee())
                .owner(owner)
                .build();

        return toResponse(gymRepository.save(gym));
    }

    @Transactional
    public GymResponse updateGym(Long gymId, GymRequest request) {
        User owner = currentUserService.getCurrentUser();
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        if (!gym.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You can update only your gym");
        }

        gym.setName(request.getName());
        gym.setLocation(request.getLocation());
        gym.setMonthlyFee(request.getMonthlyFee());
        return toResponse(gymRepository.save(gym));
    }

    public List<GymResponse> getAllGyms() {
        return gymRepository.findAll().stream().map(this::toResponse).toList();
    }

    public GymResponse getGymById(Long gymId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));
        return toResponse(gym);
    }

        public GymDetailsResponse getGymDetails(Long gymId) {
        Gym gym = gymRepository.findById(gymId)
            .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        List<EquipmentResponse> equipment = equipmentRepository.findByGym(gym).stream()
            .map(this::toEquipmentResponse)
            .toList();

        List<GymReviewResponse> reviews = gymReviewRepository.findByGymOrderByCreatedAtDesc(gym).stream()
            .map(this::toReviewResponse)
            .toList();

        double avgRating = reviews.isEmpty()
            ? 0.0
            : reviews.stream().mapToInt(GymReviewResponse::getRating).average().orElse(0.0);

        return GymDetailsResponse.builder()
            .id(gym.getId())
            .name(gym.getName())
            .location(gym.getLocation())
            .monthlyFee(gym.getMonthlyFee())
            .rating(roundToOneDecimal(avgRating))
            .reviewCount(reviews.size())
            .equipment(equipment)
            .reviews(reviews)
            .build();
        }

        @Transactional
        public GymReviewResponse addOrUpdateGymReview(Long gymId, GymReviewRequest request) {
        User user = currentUserService.getCurrentUser();
        if (user.getRole() != Role.GYM_USER) {
            throw new UnauthorizedException("Only gym users can post reviews");
        }

        Gym gym = gymRepository.findById(gymId)
            .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        Optional<GymReview> existing = gymReviewRepository.findByGymAndUser(gym, user);
        GymReview review = existing.orElseGet(() -> GymReview.builder().gym(gym).user(user).build());
        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());

        return toReviewResponse(gymReviewRepository.save(review));
        }

    public List<GymMemberResponse> getGymMembers(Long gymId) {
        User owner = currentUserService.getCurrentUser();
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        if (!gym.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Only gym owner can view members");
        }

        List<Membership> memberships = membershipRepository.findByGym(gym);
        return memberships.stream().map(m -> GymMemberResponse.builder()
                .userId(m.getUser().getId())
                .name(m.getUser().getName())
                .email(m.getUser().getEmail())
                .planName(m.getPlanName())
                .status(m.getStatus())
                .build()).toList();
    }

    private GymResponse toResponse(Gym gym) {
        List<GymReview> reviews = gymReviewRepository.findByGymOrderByCreatedAtDesc(gym);
        double avgRating = reviews.isEmpty()
                ? 0.0
                : reviews.stream().mapToInt(GymReview::getRating).average().orElse(0.0);

        return GymResponse.builder()
                .id(gym.getId())
                .name(gym.getName())
                .location(gym.getLocation())
                .monthlyFee(gym.getMonthlyFee())
                .rating(roundToOneDecimal(avgRating))
                .reviewCount(reviews.size())
                .ownerId(gym.getOwner().getId())
                .build();
    }

    private EquipmentResponse toEquipmentResponse(Equipment equipment) {
        return EquipmentResponse.builder()
                .id(equipment.getId())
                .gymId(equipment.getGym().getId())
                .equipmentName(equipment.getEquipmentName())
                .quantity(equipment.getQuantity())
                .condition(equipment.getCondition())
                .build();
    }

    private GymReviewResponse toReviewResponse(GymReview review) {
        return GymReviewResponse.builder()
                .id(review.getId())
                .gymId(review.getGym().getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private double roundToOneDecimal(double value) {
        return Double.parseDouble(String.format(Locale.US, "%.1f", value));
    }
}
