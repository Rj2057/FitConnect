package com.fitconnect.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyRevenueResponse {
    private Integer year;
    private Integer month;
    private String label;
    private BigDecimal membershipRevenue;
    private BigDecimal marketplaceRevenue;
    private BigDecimal totalRevenue;
}
