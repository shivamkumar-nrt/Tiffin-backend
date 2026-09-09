package com.tiffin.system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastNotificationDto {
    private Long id;
    private String title;
    private String message;
    private String targetAudience;
    private Long targetUserId;
    private String targetUserName;
    private String channels;
    private String sentBy;
    private int recipientsCount;
    private LocalDateTime createdAt;
    private List<Map<String, String>> whatsAppLinks; // Customer name, phone, pre-filled wa.me link
}