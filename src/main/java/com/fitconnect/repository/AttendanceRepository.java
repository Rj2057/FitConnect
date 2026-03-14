package com.fitconnect.repository;

import com.fitconnect.entity.Attendance;
import com.fitconnect.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUserAndCheckInTimeBetween(User user, LocalDateTime start, LocalDateTime end);
}
