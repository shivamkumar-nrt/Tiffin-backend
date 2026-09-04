package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.RecordStatus;
import com.tiffin.system.entity.enums.TiffinType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TiffinRecordDto {
    private Long id;
    private Long requestId;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDate serviceDate;
    private TiffinType tiffinType;
    private String menuSnapshot;
    private BigDecimal chargedAmount;
    private RecordStatus status;
    private Long paymentId;
    private LocalDateTime createdAt;
}
