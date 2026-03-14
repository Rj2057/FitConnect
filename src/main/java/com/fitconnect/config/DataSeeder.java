package com.fitconnect.config;

import com.fitconnect.entity.Gym;
import com.fitconnect.entity.Membership;
import com.fitconnect.entity.Trainer;
import com.fitconnect.entity.User;
import com.fitconnect.entity.enums.MembershipStatus;
import com.fitconnect.entity.enums.Role;
import com.fitconnect.repository.GymRepository;
import com.fitconnect.repository.MembershipRepository;
import com.fitconnect.repository.TrainerRepository;
import com.fitconnect.repository.UserRepository;
import java.time.LocalDate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
@SuppressWarnings("null")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final TrainerRepository trainerRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      GymRepository gymRepository,
                      TrainerRepository trainerRepository,
                      MembershipRepository membershipRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.gymRepository = gymRepository;
        this.trainerRepository = trainerRepository;
        this.membershipRepository = membershipRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User owner = userRepository.save(User.builder()
                .name("Default Owner")
                .email("owner@fitconnect.com")
                .password(passwordEncoder.encode("Owner@123"))
                .role(Role.GYM_OWNER)
                .build());

        User trainerUser = userRepository.save(User.builder()
                .name("Default Trainer")
                .email("trainer@fitconnect.com")
                .password(passwordEncoder.encode("Trainer@123"))
                .role(Role.GYM_TRAINER)
                .build());

        User gymUser = userRepository.save(User.builder()
                .name("Default User")
                .email("user@fitconnect.com")
                .password(passwordEncoder.encode("User@123"))
                .role(Role.GYM_USER)
                .build());

        Gym gym = gymRepository.save(Gym.builder()
                .name("FitConnect Arena")
                .location("Bengaluru")
                .monthlyFee(1999.0)
                .owner(owner)
                .build());

        trainerRepository.save(Trainer.builder()
                .user(trainerUser)
                .gym(gym)
                .experience(4)
                .specialization("Strength and Conditioning")
                .rating(4.6)
                .build());

        membershipRepository.save(Membership.builder()
                .user(gymUser)
                .gym(gym)
                .planName("BASIC")
                .durationMonths(1)
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now().plusDays(23))
                .status(MembershipStatus.ACTIVE)
                .build());
    }
}
