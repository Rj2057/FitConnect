package com.fitconnect.repository;

import com.fitconnect.entity.Gym;
import com.fitconnect.entity.Payment;
import com.fitconnect.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	List<Payment> findByUser(User user);
	List<Payment> findByGym(Gym gym);
}
