package com.fitconnect.service;

import com.fitconnect.dto.TrainerProfileUpdateRequest;
import com.fitconnect.dto.TrainerResponse;
import com.fitconnect.entity.Gym;
import com.fitconnect.entity.Trainer;
import com.fitconnect.entity.User;
import com.fitconnect.exception.ResourceNotFoundException;
import com.fitconnect.repository.GymRepository;
import com.fitconnect.repository.TrainerBookingRepository;
import com.fitconnect.repository.TrainerRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final GymRepository gymRepository;
    private final TrainerBookingRepository bookingRepository;
    private final CurrentUserService currentUserService;

    public TrainerService(TrainerRepository trainerRepository,
                          GymRepository gymRepository,
                          TrainerBookingRepository bookingRepository,
                          CurrentUserService currentUserService) {
        this.trainerRepository = trainerRepository;
        this.gymRepository = gymRepository;
        this.bookingRepository = bookingRepository;
        this.currentUserService = currentUserService;
    }

    public List<TrainerResponse> getAllTrainers() {
        return trainerRepository.findAll().stream().map(this::toResponse).toList();
    }

    public TrainerResponse getTrainerById(Long id) {
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
        return toResponse(trainer);
    }

    @Transactional
    public TrainerResponse updateProfile(TrainerProfileUpdateRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Gym gym = gymRepository.findById(request.getGymId())
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        Trainer trainer = trainerRepository.findByUser(currentUser)
                .orElseGet(() -> Trainer.builder().user(currentUser).build());

        trainer.setGym(gym);
        trainer.setExperience(request.getExperience());
        trainer.setSpecialization(request.getSpecialization());
        if (trainer.getRating() == null) {
            trainer.setRating(0.0);
        }

        return toResponse(trainerRepository.save(trainer));
    }

    @Transactional
    public void refreshTrainerRating(Trainer trainer) {
        List<Integer> ratings = bookingRepository.findByTrainerAndUserRatingNotNull(trainer).stream()
                .map(item -> item.getUserRating())
                .toList();

        if (ratings.isEmpty()) {
            trainer.setRating(0.0);
        } else {
            double average = ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            trainer.setRating(Double.parseDouble(String.format(Locale.US, "%.1f", average)));
        }

        trainerRepository.save(trainer);
    }

    public Trainer getTrainerEntity(Long id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
    }

    public Trainer getCurrentTrainer() {
        User currentUser = currentUserService.getCurrentUser();
        return trainerRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer profile not found"));
    }

    private TrainerResponse toResponse(Trainer trainer) {
        return TrainerResponse.builder()
                .id(trainer.getId())
                .userId(trainer.getUser().getId())
                .trainerName(trainer.getUser().getName())
                .gymId(trainer.getGym().getId())
                .gymName(trainer.getGym().getName())
                .experience(trainer.getExperience())
                .specialization(trainer.getSpecialization())
                .rating(trainer.getRating())
                .ratingCount(bookingRepository.countByTrainerAndUserRatingNotNull(trainer))
                .build();
    }
}
