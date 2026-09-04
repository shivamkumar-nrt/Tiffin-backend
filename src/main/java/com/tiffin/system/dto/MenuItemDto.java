package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.ItemCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemDto {
    private Long id;
    
    @NotBlank(message = "Item name is required")
    private String name;

    @NotNull(message = "Category is required")
    private ItemCategory category;

    private String description;
    private boolean isSpicy;
    private boolean isSweet;
}
