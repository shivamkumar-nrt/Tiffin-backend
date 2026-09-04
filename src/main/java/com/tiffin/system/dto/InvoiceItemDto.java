package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.TiffinType;
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
public class InvoiceItemDto {
    private Long id;
    private Long tiffinRecordId;
    private LocalDate serviceDate;
    private TiffinType tiffinType;
    private String menuSummary;
    private BigDecimal chargedAmount;
}
