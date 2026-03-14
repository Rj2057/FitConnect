package com.fitconnect.dto;

import com.fitconnect.entity.enums.MembershipStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GymMemberResponse {
    private Long userId;
    private String name;
    private String email;
    private String planName;
    private MembershipStatus status;
}
