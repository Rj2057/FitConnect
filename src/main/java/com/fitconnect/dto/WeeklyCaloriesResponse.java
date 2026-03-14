package com.fitconnect.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeeklyCaloriesResponse {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private BigDecimal totalCalories;
}
