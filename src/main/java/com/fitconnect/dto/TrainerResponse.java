package com.fitconnect.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainerResponse {
    private Long id;
    private Long userId;
    private String trainerName;
    private Long gymId;
    private String gymName;
    private Integer experience;
    private String specialization;
    private Double rating;
    private Long ratingCount;
}
