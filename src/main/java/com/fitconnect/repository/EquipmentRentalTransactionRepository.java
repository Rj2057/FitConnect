package com.fitconnect.repository;

import com.fitconnect.entity.EquipmentRentalTransaction;
import com.fitconnect.entity.Gym;
import com.fitconnect.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRentalTransactionRepository extends JpaRepository<EquipmentRentalTransaction, Long> {
    List<EquipmentRentalTransaction> findBySellerOwnerOrBuyerOwner(User sellerOwner, User buyerOwner);
    List<EquipmentRentalTransaction> findBySellerGym(Gym sellerGym);
    List<EquipmentRentalTransaction> findBySellerGymOrBuyerGym(Gym sellerGym, Gym buyerGym);
}
