package com.fitconnect.service;

import com.fitconnect.dto.StreakResponse;
import com.fitconnect.entity.Streak;
import com.fitconnect.entity.User;
import com.fitconnect.exception.ResourceNotFoundException;
import com.fitconnect.repository.StreakRepository;
import com.fitconnect.repository.UserRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class StreakService {

    private static final int MONTHLY_PAUSE_TOKENS = 3;

    private final StreakRepository streakRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public StreakService(StreakRepository streakRepository,
                         UserRepository userRepository,
                         CurrentUserService currentUserService) {
        this.streakRepository = streakRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public StreakResponse updateForCurrentUser() {
        User user = currentUserService.getCurrentUser();
        Streak streak = updateStreakForUser(user, LocalDate.now());
        return toResponse(streak);
    }

    @Transactional
    public void updateForUserOnActivity(User user, LocalDate activityDate) {
        updateStreakForUser(user, activityDate);
    }

    public StreakResponse getByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Streak streak = streakRepository.findByUser(user)
                .orElseGet(() -> initializeStreak(user));

        return toResponse(streak);
    }

    private Streak updateStreakForUser(User user, LocalDate activityDate) {
        Streak streak = streakRepository.findByUser(user).orElseGet(() -> initializeStreak(user));
        resetMonthlyTokensIfRequired(streak, activityDate);

        if (streak.getLastActivityDate() == null) {
            streak.setStreakCount(1);
            streak.setLastActivityDate(activityDate);
            return streakRepository.save(streak);
        }

        long gapDays = ChronoUnit.DAYS.between(streak.getLastActivityDate(), activityDate);

        if (gapDays <= 0) {
            return streak;
        }

        if (gapDays == 1) {
            streak.setStreakCount(streak.getStreakCount() + 1);
            streak.setLastActivityDate(activityDate);
            return streakRepository.save(streak);
        }

        int missedDays = (int) gapDays - 1;
        if (streak.getPauseTokensRemaining() >= missedDays) {
            streak.setPauseTokensRemaining(streak.getPauseTokensRemaining() - missedDays);
            streak.setStreakCount(streak.getStreakCount() + 1);
        } else {
            streak.setStreakCount(1);
        }

        streak.setLastActivityDate(activityDate);
        return streakRepository.save(streak);
    }

    private void resetMonthlyTokensIfRequired(Streak streak, LocalDate now) {
        YearMonth current = YearMonth.from(now);
        if (!current.equals(YearMonth.of(streak.getPauseResetYear(), streak.getPauseResetMonth()))) {
            streak.setPauseTokensRemaining(MONTHLY_PAUSE_TOKENS);
            streak.setPauseResetMonth(current.getMonthValue());
            streak.setPauseResetYear(current.getYear());
        }
    }

    private Streak initializeStreak(User user) {
        LocalDate now = LocalDate.now();
        Streak streak = Streak.builder()
                .user(user)
                .streakCount(0)
                .pauseTokensRemaining(MONTHLY_PAUSE_TOKENS)
                .lastActivityDate(now.minusDays(1))
                .pauseResetMonth(now.getMonthValue())
                .pauseResetYear(now.getYear())
                .build();
        return streakRepository.save(streak);
    }

    private StreakResponse toResponse(Streak streak) {
        return StreakResponse.builder()
                .userId(streak.getUser().getId())
                .streakCount(streak.getStreakCount())
                .pauseTokensRemaining(streak.getPauseTokensRemaining())
                .lastActivityDate(streak.getLastActivityDate())
                .build();
    }
}
