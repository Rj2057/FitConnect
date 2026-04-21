package com.fitconnect.service;

import com.fitconnect.dto.StreakResponse;
import com.fitconnect.entity.Membership;
import com.fitconnect.entity.Streak;
import com.fitconnect.entity.User;
import com.fitconnect.entity.enums.MembershipStatus;
import com.fitconnect.exception.BadRequestException;
import com.fitconnect.exception.ResourceNotFoundException;
import com.fitconnect.repository.MembershipRepository;
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

    private static final int DEFAULT_MONTHLY_PAUSE_TOKENS = 3;

    private final StreakRepository streakRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public StreakService(StreakRepository streakRepository,
                         MembershipRepository membershipRepository,
                         UserRepository userRepository,
                         CurrentUserService currentUserService) {
        this.streakRepository = streakRepository;
        this.membershipRepository = membershipRepository;
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

    @Transactional
    public StreakResponse usePauseForCurrentUser() {
        User user = currentUserService.getCurrentUser();
        LocalDate today = LocalDate.now();
        Streak streak = streakRepository.findByUser(user).orElseGet(() -> initializeStreak(user));

        resetMonthlyTokensIfRequired(streak, today);

        long gapDays = ChronoUnit.DAYS.between(streak.getLastActivityDate(), today);
        if (gapDays <= 0) {
            throw new BadRequestException("Pause can only be used once per day");
        }

        int requiredTokens = (int) gapDays;
        if (streak.getPauseTokensRemaining() < requiredTokens) {
            throw new BadRequestException("Not enough pause tokens remaining");
        }

        streak.setPauseTokensRemaining(streak.getPauseTokensRemaining() - requiredTokens);
        streak.setLastActivityDate(today);
        extendMembershipExpiryForPause(user, requiredTokens, today);

        return toResponse(streakRepository.save(streak));
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
            streak.setPauseTokensRemaining(getPauseTokensForMembership(streak.getUser(), now));
            streak.setPauseResetMonth(current.getMonthValue());
            streak.setPauseResetYear(current.getYear());
        }
    }

    private Streak initializeStreak(User user) {
        LocalDate now = LocalDate.now();
        Streak streak = Streak.builder()
                .user(user)
                .streakCount(0)
                .pauseTokensRemaining(getPauseTokensForMembership(user, now))
                .lastActivityDate(now.minusDays(1))
                .pauseResetMonth(now.getMonthValue())
                .pauseResetYear(now.getYear())
                .build();
        return streakRepository.save(streak);
    }

    private int getPauseTokensForMembership(User user, LocalDate now) {
        return membershipRepository.findByUser(user).stream()
                .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE
                        && !membership.getEndDate().isBefore(now))
                .map(Membership::getDurationMonths)
                .max(Integer::compareTo)
                .map(months -> Math.max(DEFAULT_MONTHLY_PAUSE_TOKENS, months * 3))
                .orElse(DEFAULT_MONTHLY_PAUSE_TOKENS);
    }

    private void extendMembershipExpiryForPause(User user, int days, LocalDate now) {
        membershipRepository.findByUser(user).stream()
                .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE
                        && !membership.getEndDate().isBefore(now))
                .forEach(membership -> membership.setEndDate(membership.getEndDate().plusDays(days)));
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
