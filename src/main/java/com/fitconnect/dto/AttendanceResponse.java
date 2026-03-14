package com.fitconnect.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceResponse {
    private Long id;
    private Long userId;
    private Long gymId;
    private LocalDateTime checkInTime;
}
