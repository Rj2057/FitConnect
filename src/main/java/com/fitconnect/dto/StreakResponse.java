package com.fitconnect.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StreakResponse {
    private Long userId;
    private Integer streakCount;
    private Integer pauseTokensRemaining;
    private LocalDate lastActivityDate;
}
