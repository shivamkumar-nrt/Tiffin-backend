package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.TiffinType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ManualTiffinRecordRequest {
    private Long userId;
    private LocalDate serviceDate;
    private TiffinType tiffinType;
    private BigDecimal amount;
    private String menuSnapshot;
}
