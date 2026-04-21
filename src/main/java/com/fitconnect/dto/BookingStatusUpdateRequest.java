package com.fitconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookingStatusUpdateRequest {

    @NotBlank
    private String action;

    private String message;

    private String proposedTimeSlot;
}
