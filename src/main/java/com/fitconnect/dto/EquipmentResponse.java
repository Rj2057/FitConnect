package com.fitconnect.dto;

import com.fitconnect.entity.enums.EquipmentCondition;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EquipmentResponse {
    private Long id;
    private Long gymId;
    private String equipmentName;
    private Integer quantity;
    private EquipmentCondition condition;
}
