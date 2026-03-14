package com.fitconnect.repository;

import com.fitconnect.entity.Equipment;
import com.fitconnect.entity.Gym;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByGym(Gym gym);
}
