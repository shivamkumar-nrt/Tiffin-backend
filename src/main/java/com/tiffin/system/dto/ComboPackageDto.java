package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.TiffinType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComboPackageDto {
    private Long id;

    @NotBlank(message = "Combo name is required")
    private String name;

    @NotNull(message = "Tiffin category type is required")
    private TiffinType tiffinType;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    private String description;

    @Builder.Default
    private List<String> includedItems = new ArrayList<>();

    private boolean active;
    private LocalDateTime createdAt;
}