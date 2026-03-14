package com.fitconnect.repository;

import com.fitconnect.entity.Gym;
import com.fitconnect.entity.Membership;
import com.fitconnect.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByGym(Gym gym);
    List<Membership> findByUser(User user);
}
