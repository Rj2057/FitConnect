package com.fitconnect.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GymDetailsResponse {
    private Long id;
    private String name;
    private String location;
    private Double monthlyFee;
    private Double rating;
    private Integer reviewCount;
    private List<EquipmentResponse> equipment;
    private List<GymReviewResponse> reviews;
}