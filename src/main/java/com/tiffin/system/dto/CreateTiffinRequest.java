package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.TiffinType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTiffinRequest {
    private Long userId; // Optional for admin submitting on behalf, required otherwise from JWT

    @NotNull(message = "Service date is required")
    private LocalDate serviceDate;

    @NotNull(message = "Tiffin type is required")
    private TiffinType tiffinType;

    private Long comboId; // Optional ID of selected Combo Package
    private String comboName; // Optional Name of selected Combo

    private String specialInstructions;
}