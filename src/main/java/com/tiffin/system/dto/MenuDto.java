package com.tiffin.system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuDto {
    private Long id;

    @NotNull(message = "Service date is required")
    private LocalDate serviceDate;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private boolean isSpecial;

    @Valid
    @Builder.Default
    private List<MenuItemDto> items = new ArrayList<>();
}
