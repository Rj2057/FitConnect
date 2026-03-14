package com.fitconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrainerProfileUpdateRequest {

    @NotNull
    private Long gymId;

    @NotNull
    private Integer experience;

    @NotBlank
    private String specialization;
}
