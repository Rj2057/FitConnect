package com.fitconnect.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkoutResponse {
    private Long id;
    private Long userId;
    private String exerciseName;
    private BigDecimal weight;
    private Integer reps;
    private Integer duration;
    private BigDecimal caloriesBurned;
    private LocalDateTime createdAt;
}
