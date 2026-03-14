package com.fitconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull
    private Long trainerId;

    @NotNull
    private LocalDate date;

    @NotBlank
    private String timeSlot;
}
