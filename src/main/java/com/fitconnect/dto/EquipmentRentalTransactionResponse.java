package com.fitconnect.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EquipmentRentalTransactionResponse {
    private Long id;
    private Long sellerOwnerId;
    private String sellerOwnerName;
    private String sellerOwnerEmail;
    private Long buyerOwnerId;
    private String buyerOwnerName;
    private String buyerOwnerEmail;
    private Long sellerGymId;
    private String sellerGymName;
    private Long buyerGymId;
    private String buyerGymName;
    private String equipmentName;
    private String details;
    private BigDecimal monthlyRentPrice;
    private LocalDateTime purchasedAt;
}
