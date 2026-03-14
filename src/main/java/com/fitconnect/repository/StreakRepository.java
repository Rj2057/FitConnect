package com.fitconnect.repository;

import com.fitconnect.entity.Streak;
import com.fitconnect.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreakRepository extends JpaRepository<Streak, Long> {
    Optional<Streak> findByUser(User user);
}
