package com.fitconnect.repository;

import com.fitconnect.entity.EquipmentRentalListing;
import com.fitconnect.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRentalListingRepository extends JpaRepository<EquipmentRentalListing, Long> {
    List<EquipmentRentalListing> findBySellerOwner(User sellerOwner);
}
