package com.fitconnect.repository;

import com.fitconnect.entity.Gym;
import com.fitconnect.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymRepository extends JpaRepository<Gym, Long> {
    List<Gym> findByOwner(User owner);
}
