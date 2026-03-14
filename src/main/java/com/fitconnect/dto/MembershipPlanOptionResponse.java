package com.fitconnect.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MembershipPlanOptionResponse {
    private String planName;
    private String description;
    private Double multiplier;
    private Integer minimumMonths;
}