package com.fitconnect.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EquipmentRentalListingResponse {
    private Long id;
    private Long sellerOwnerId;
    private String sellerOwnerName;
    private String sellerOwnerEmail;
    private Long sellerGymId;
    private String sellerGymName;
    private String equipmentName;
    private String details;
    private BigDecimal monthlyRentPrice;
    private LocalDateTime createdAt;
}
