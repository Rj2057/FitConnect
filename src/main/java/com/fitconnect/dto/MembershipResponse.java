package com.fitconnect.dto;

import com.fitconnect.entity.enums.MembershipStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MembershipResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long gymId;
    private String gymName;
    private String planName;
    private Integer durationMonths;
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private MembershipStatus status;
}
