package com.tiffin.system.controller;

import com.tiffin.system.dto.ApiResponse;
import com.tiffin.system.dto.BroadcastNotificationDto;
import com.tiffin.system.dto.BroadcastRequest;
import com.tiffin.system.service.NotificationBroadcastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationBroadcastService broadcastService;

    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BroadcastNotificationDto>> sendBroadcast(
            @Valid @RequestBody BroadcastRequest request,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        BroadcastNotificationDto result = broadcastService.sendBroadcast(request, adminEmail);
        return ResponseEntity.ok(ApiResponse.ok(
                "Broadcast sent successfully to " + result.getRecipientsCount() + " customers.",
                result
        ));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BroadcastNotificationDto>>> getBroadcastHistory() {
        List<BroadcastNotificationDto> history = broadcastService.getBroadcastHistory();
        return ResponseEntity.ok(ApiResponse.ok("Broadcast history fetched", history));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BroadcastNotificationDto>>> getMyNotifications(Authentication authentication) {
        String userEmail = authentication.getName();
        List<BroadcastNotificationDto> list = broadcastService.getMyNotifications(userEmail);
        return ResponseEntity.ok(ApiResponse.ok("Notifications fetched", list));
    }
}