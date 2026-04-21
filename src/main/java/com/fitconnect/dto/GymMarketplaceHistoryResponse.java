package com.fitconnect.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GymMarketplaceHistoryResponse {
    private Integer year;
    private Integer month;
    private String label;
    private Integer soldCount;
    private Integer rentedCount;
    private BigDecimal soldAmount;
    private BigDecimal rentedAmount;
}
