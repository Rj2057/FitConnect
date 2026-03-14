package com.fitconnect.repository;

import com.fitconnect.entity.Trainer;
import com.fitconnect.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUser(User user);
}
