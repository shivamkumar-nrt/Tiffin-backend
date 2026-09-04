package com.tiffin.system.controller;

import com.tiffin.system.dto.ApiResponse;
import com.tiffin.system.dto.DashboardStatsDto;
import com.tiffin.system.dto.UserDashboardStatsDto;
import com.tiffin.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getAdminStats() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getAdminDashboardStats()));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<UserDashboardStatsDto>> getUserStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getUserDashboardStats(userDetails.getUsername())));
    }
}
