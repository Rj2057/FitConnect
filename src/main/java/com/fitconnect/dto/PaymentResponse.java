package com.fitconnect.dto;

import com.fitconnect.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long gymId;
    private String gymName;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime paidAt;
}
