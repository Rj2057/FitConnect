package com.fitconnect.repository;

import com.fitconnect.entity.Trainer;
import com.fitconnect.entity.TrainerBooking;
import com.fitconnect.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerBookingRepository extends JpaRepository<TrainerBooking, Long> {
    List<TrainerBooking> findByUser(User user);
    List<TrainerBooking> findByTrainer(Trainer trainer);
    List<TrainerBooking> findByTrainerAndUserRatingNotNull(Trainer trainer);
    long countByTrainerAndUserRatingNotNull(Trainer trainer);
    Optional<TrainerBooking> findByIdAndUser(Long id, User user);
}
