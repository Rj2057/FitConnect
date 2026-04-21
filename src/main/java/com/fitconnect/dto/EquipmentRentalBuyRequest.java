package com.fitconnect.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipmentRentalBuyRequest {

    @NotNull
    private Long buyerGymId;
}
