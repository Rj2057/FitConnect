package com.fitconnect.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GymResponse {
    private Long id;
    private String name;
    private String location;
    private Double monthlyFee;
    private Double rating;
    private Integer reviewCount;
    private Long ownerId;
}
