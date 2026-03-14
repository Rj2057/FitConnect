package com.fitconnect.dto;

import com.fitconnect.entity.enums.EquipmentCondition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipmentRequest {

    @NotNull
    private Long gymId;

    @NotBlank
    private String equipmentName;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private EquipmentCondition condition;
}
