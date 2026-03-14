package com.fitconnect.repository;

import com.fitconnect.entity.User;
import com.fitconnect.entity.Workout;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByUserOrderByCreatedAtDesc(User user);
    List<Workout> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
}
