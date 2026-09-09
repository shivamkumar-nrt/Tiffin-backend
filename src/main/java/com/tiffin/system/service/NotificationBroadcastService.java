package com.tiffin.system.service;

import com.tiffin.system.dto.BroadcastNotificationDto;
import com.tiffin.system.dto.BroadcastRequest;

import java.util.List;

public interface NotificationBroadcastService {
    BroadcastNotificationDto sendBroadcast(BroadcastRequest request, String adminEmail);
    List<BroadcastNotificationDto> getBroadcastHistory();
    List<BroadcastNotificationDto> getMyNotifications(String userEmail);
}