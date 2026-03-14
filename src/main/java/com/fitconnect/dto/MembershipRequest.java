package com.fitconnect.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MembershipRequest {

    @NotNull
    private Long gymId;

    @NotNull
    private String planName;

    @NotNull
    @Min(1)
    private Integer durationMonths;
}
