package com.fitconnect.dto;

import com.fitconnect.entity.enums.MembershipStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MembershipStatusUpdateRequest {

    @NotNull
    private MembershipStatus status;
}
