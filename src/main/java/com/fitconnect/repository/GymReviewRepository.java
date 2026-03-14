package com.fitconnect.repository;

import com.fitconnect.entity.Gym;
import com.fitconnect.entity.GymReview;
import com.fitconnect.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymReviewRepository extends JpaRepository<GymReview, Long> {
    List<GymReview> findByGymOrderByCreatedAtDesc(Gym gym);
    Optional<GymReview> findByGymAndUser(Gym gym, User user);
}