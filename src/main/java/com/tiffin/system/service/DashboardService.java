package com.tiffin.system.service;

import com.tiffin.system.dto.DashboardStatsDto;
import com.tiffin.system.dto.UserDashboardStatsDto;

public interface DashboardService {
    DashboardStatsDto getAdminDashboardStats();
    UserDashboardStatsDto getUserDashboardStats(String userEmail);
}
