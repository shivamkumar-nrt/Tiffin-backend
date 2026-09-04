package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.TiffinType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceConfigDto {
    private Long id;

    @NotNull(message = "Tiffin type is required")
    private TiffinType tiffinType;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
    private boolean isActive;
}
