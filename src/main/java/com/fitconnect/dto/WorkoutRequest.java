package com.fitconnect.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class WorkoutRequest {

    @NotBlank
    private String exerciseName;

    @NotNull
    @Min(0)
    private BigDecimal weight;

    @NotNull
    @Min(0)
    private Integer reps;

    @NotNull
    @Min(1)
    private Integer duration;
}
