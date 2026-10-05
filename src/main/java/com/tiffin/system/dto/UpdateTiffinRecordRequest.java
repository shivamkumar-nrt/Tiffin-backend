package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.RecordStatus;
import com.tiffin.system.entity.enums.TiffinType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateTiffinRecordRequest {
    private LocalDate serviceDate;
    private TiffinType tiffinType;
    private BigDecimal amount;
    private String menuSnapshot;
    private RecordStatus status;
}
