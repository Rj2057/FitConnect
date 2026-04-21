package com.fitconnect.service;

import com.fitconnect.dto.AttendanceCheckInRequest;
import com.fitconnect.dto.AttendanceResponse;
import com.fitconnect.entity.Attendance;
import com.fitconnect.entity.Gym;
import com.fitconnect.entity.Membership;
import com.fitconnect.entity.Streak;
import com.fitconnect.entity.User;
import com.fitconnect.exception.BadRequestException;
import com.fitconnect.exception.ResourceNotFoundException;
import com.fitconnect.repository.AttendanceRepository;
import com.fitconnect.repository.GymRepository;
import com.fitconnect.repository.MembershipRepository;
import com.fitconnect.repository.StreakRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final GymRepository gymRepository;
    private final MembershipRepository membershipRepository;
    private final StreakRepository streakRepository;
    private final CurrentUserService currentUserService;
    private final StreakService streakService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             GymRepository gymRepository,
                             MembershipRepository membershipRepository,
                             StreakRepository streakRepository,
                             CurrentUserService currentUserService,
                             StreakService streakService) {
        this.attendanceRepository = attendanceRepository;
        this.gymRepository = gymRepository;
        this.membershipRepository = membershipRepository;
        this.streakRepository = streakRepository;
        this.currentUserService = currentUserService;
        this.streakService = streakService;
    }

    @Transactional
    public AttendanceResponse checkIn(AttendanceCheckInRequest request) {
        User user = currentUserService.getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime dayEnd = LocalDateTime.of(today, LocalTime.MAX);
        boolean alreadyCheckedInToday = !attendanceRepository.findByUserAndCheckInTimeBetween(user, dayStart, dayEnd).isEmpty();
        if (alreadyCheckedInToday) {
            throw new BadRequestException("You have already checked in today. Next check-in is available tomorrow.");
        }

        Streak streak = streakRepository.findByUser(user).orElse(null);
        if (streak != null && today.equals(streak.getLastActivityDate())) {
            throw new BadRequestException("You used pause/activity today. Check-in is available from tomorrow.");
        }

        Gym gym = resolveGymForAttendance(user, request);

        Attendance attendance = Attendance.builder()
                .user(user)
                .gym(gym)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        streakService.updateForUserOnActivity(user, today);

        return AttendanceResponse.builder()
                .id(saved.getId())
                .userId(saved.getUser().getId())
                .gymId(saved.getGym().getId())
                .checkInTime(saved.getCheckInTime())
                .build();
    }

    private Gym resolveGymForAttendance(User user, AttendanceCheckInRequest request) {
        if (request != null && request.getGymId() != null) {
            return gymRepository.findById(request.getGymId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));
        }

        return membershipRepository.findByUser(user).stream()
                .filter(membership -> membership.getStatus().name().equals("ACTIVE"))
                .filter(membership -> !membership.getEndDate().isBefore(LocalDate.now()))
                .max(Comparator.comparing(Membership::getEndDate))
                .map(Membership::getGym)
                .orElseThrow(() -> new ResourceNotFoundException("No active gym membership found for attendance"));
    }
}
