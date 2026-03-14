package com.fitconnect.service;

import com.fitconnect.dto.WeeklyCaloriesResponse;
import com.fitconnect.dto.WorkoutRequest;
import com.fitconnect.dto.WorkoutResponse;
import com.fitconnect.entity.User;
import com.fitconnect.entity.Workout;
import com.fitconnect.repository.WorkoutRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final CurrentUserService currentUserService;
    private final StreakService streakService;

    public WorkoutService(WorkoutRepository workoutRepository,
                          CurrentUserService currentUserService,
                          StreakService streakService) {
        this.workoutRepository = workoutRepository;
        this.currentUserService = currentUserService;
        this.streakService = streakService;
    }

    @Transactional
    public WorkoutResponse logWorkout(WorkoutRequest request) {
        User user = currentUserService.getCurrentUser();
        BigDecimal calories = calculateCalories(request.getExerciseName(), request.getDuration(), request.getWeight());

        Workout workout = Workout.builder()
                .user(user)
                .exerciseName(request.getExerciseName())
                .weight(request.getWeight())
                .reps(request.getReps())
                .duration(request.getDuration())
                .caloriesBurned(calories)
                .build();

        Workout saved = workoutRepository.save(workout);
        streakService.updateForUserOnActivity(user, LocalDate.now());

        return toResponse(saved);
    }

    public List<WorkoutResponse> getWorkoutHistory() {
        User user = currentUserService.getCurrentUser();
        return workoutRepository.findByUserOrderByCreatedAtDesc(user).stream().map(this::toResponse).toList();
    }

    public WeeklyCaloriesResponse getWeeklyCalories() {
        User user = currentUserService.getCurrentUser();

        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        LocalDateTime startDateTime = LocalDateTime.of(weekStart, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(weekEnd, LocalTime.MAX);

        BigDecimal total = workoutRepository.findByUserAndCreatedAtBetween(user, startDateTime, endDateTime)
                .stream()
                .map(Workout::getCaloriesBurned)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return WeeklyCaloriesResponse.builder()
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .totalCalories(total)
                .build();
    }

    private BigDecimal calculateCalories(String exerciseName, Integer durationMinutes, BigDecimal weightKg) {
        String exercise = exerciseName.toLowerCase(Locale.ROOT);
        BigDecimal met;

        if (exercise.contains("run") || exercise.contains("treadmill")) {
            met = BigDecimal.valueOf(9.8);
        } else if (exercise.contains("cycle") || exercise.contains("bike")) {
            met = BigDecimal.valueOf(7.5);
        } else if (exercise.contains("yoga")) {
            met = BigDecimal.valueOf(3.0);
        } else if (exercise.contains("swim")) {
            met = BigDecimal.valueOf(8.3);
        } else {
            met = BigDecimal.valueOf(6.0);
        }

        BigDecimal hours = BigDecimal.valueOf(durationMinutes).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return met.multiply(weightKg).multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }

    private WorkoutResponse toResponse(Workout workout) {
        return WorkoutResponse.builder()
                .id(workout.getId())
                .userId(workout.getUser().getId())
                .exerciseName(workout.getExerciseName())
                .weight(workout.getWeight())
                .reps(workout.getReps())
                .duration(workout.getDuration())
                .caloriesBurned(workout.getCaloriesBurned())
                .createdAt(workout.getCreatedAt())
                .build();
    }
}
