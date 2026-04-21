package com.fitconnect.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class EquipmentRentalListingRequest {

    @NotNull
    private Long sellerGymId;

    @NotBlank
    private String equipmentName;

    private String details;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal monthlyRentPrice;
}
