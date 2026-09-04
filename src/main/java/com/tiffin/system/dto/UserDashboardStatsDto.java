package com.tiffin.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDashboardStatsDto {
    private BigDecimal outstandingBalance;
    private long totalTiffinsConsumed;
    private long pendingRequestsCount;
    private MenuDto todayMenu;
    private PriceConfigDto currentFullPrice;
    private PriceConfigDto currentHalfPrice;
    private List<TiffinRequestDto> recentRequests;
    private List<TiffinRecordDto> recentRecords;
    private List<InvoiceDto> recentInvoices;
}
