package com.tiffin.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDto {
    private long totalEmployees;
    private long activeEmployees;
    private long pendingRequests;
    private long approvedRequestsToday;
    private long totalTiffinsToday;
    private BigDecimal totalOutstandingDues;
    private BigDecimal totalRevenueCollected;
    private BigDecimal todayRevenue;
    private PriceConfigDto currentFullPrice;
    private PriceConfigDto currentHalfPrice;
    private MenuDto todayMenu;
    private List<TiffinRequestDto> recentPendingRequests;
    private List<PaymentDto> recentPayments;
}
