package com.tiffin.system.service.impl;

import com.tiffin.system.dto.BroadcastNotificationDto;
import com.tiffin.system.dto.BroadcastRequest;
import com.tiffin.system.entity.BroadcastNotification;
import com.tiffin.system.entity.TiffinRecord;
import com.tiffin.system.entity.User;
import com.tiffin.system.entity.enums.RecordStatus;
import com.tiffin.system.entity.enums.RoleType;
import com.tiffin.system.entity.enums.UserStatus;
import com.tiffin.system.exception.BadRequestException;
import com.tiffin.system.repository.BroadcastNotificationRepository;
import com.tiffin.system.repository.TiffinRecordRepository;
import com.tiffin.system.repository.UserRepository;
import com.tiffin.system.service.AuditService;
import com.tiffin.system.service.EmailService;
import com.tiffin.system.service.NotificationBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationBroadcastServiceImpl implements NotificationBroadcastService {

    private final UserRepository userRepository;
    private final TiffinRecordRepository tiffinRecordRepository;
    private final BroadcastNotificationRepository broadcastRepository;
    private final EmailService emailService;
    private final AuditService auditService;

    @Override
    @Transactional
    public BroadcastNotificationDto sendBroadcast(BroadcastRequest request, String adminEmail) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Notification title / subject is required");
        }
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new BadRequestException("Notification message body is required");
        }

        String targetAudience = request.getTargetAudience() != null ? request.getTargetAudience().toUpperCase() : "ALL";
        List<User> recipients = new ArrayList<>();
        String targetUserName = null;

        if ("SINGLE".equals(targetAudience) && request.getTargetUserId() != null) {
            User targetUser = userRepository.findById(request.getTargetUserId())
                    .orElseThrow(() -> new BadRequestException("Target customer not found with id: " + request.getTargetUserId()));
            recipients.add(targetUser);
            targetUserName = targetUser.getFullName();
        } else if ("WITH_DUES".equals(targetAudience)) {
            Set<Long> userIdsWithDues = tiffinRecordRepository.findAll().stream()
                    .filter(r -> r.getStatus() == RecordStatus.UNPAID)
                    .map(r -> r.getUser().getId())
                    .collect(Collectors.toSet());
            recipients = userRepository.findAllById(userIdsWithDues).stream()
                    .filter(u -> u.getStatus() == UserStatus.ACTIVE && u.getRole() == RoleType.ROLE_EMPLOYEE)
                    .collect(Collectors.toList());
        } else {
            // ALL active customers
            recipients = userRepository.findAll().stream()
                    .filter(u -> u.getStatus() == UserStatus.ACTIVE && u.getRole() == RoleType.ROLE_EMPLOYEE)
                    .collect(Collectors.toList());
            targetAudience = "ALL";
        }

        List<String> activeChannels = new ArrayList<>();
        if (request.isSendEmail()) activeChannels.add("EMAIL");
        if (request.isSendWhatsApp()) activeChannels.add("WHATSAPP");
        if (request.isSendPush()) activeChannels.add("PUSH");
        String channelsStr = String.join(",", activeChannels);

        // 1. Dispatch HTML Emails
        if (request.isSendEmail()) {
            for (User recipient : recipients) {
                if (recipient.getEmail() != null && !recipient.getEmail().trim().isEmpty()) {
                    String personalizedHtml = buildHtmlEmail(request.getTitle(), request.getMessage(), recipient);
                    emailService.sendEmail(recipient.getEmail(), request.getTitle(), personalizedHtml);
                }
            }
        }

        // 2. Generate WhatsApp 1-Click Links
        List<Map<String, String>> whatsAppLinks = new ArrayList<>();
        if (request.isSendWhatsApp()) {
            for (User recipient : recipients) {
                if (recipient.getPhone() != null && !recipient.getPhone().trim().isEmpty()) {
                    String phoneClean = recipient.getPhone().replaceAll("[\\s\\-\\+\\(\\)]", "");
                    if (phoneClean.length() == 10) phoneClean = "91" + phoneClean;

                    String personalizedText = "Namaste " + recipient.getFullName() + " ji 🙏\n\n"
                            + "📢 *" + request.getTitle() + "*\n"
                            + "-----------------------------------------\n"
                            + request.getMessage() + "\n\n"
                            + "🍱 _Tiffin Service Management System_";

                    String encoded = URLEncoder.encode(personalizedText, StandardCharsets.UTF_8);
                    String link = "https://wa.me/" + phoneClean + "?text=" + encoded;

                    Map<String, String> item = new HashMap<>();
                    item.put("userId", recipient.getId().toString());
                    item.put("customerName", recipient.getFullName());
                    item.put("phone", recipient.getPhone());
                    item.put("whatsappLink", link);
                    whatsAppLinks.add(item);
                }
            }
        }

        // 3. Save Broadcast Record
        BroadcastNotification record = BroadcastNotification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .targetAudience(targetAudience)
                .targetUserId("SINGLE".equals(targetAudience) ? request.getTargetUserId() : null)
                .targetUserName(targetUserName)
                .channels(channelsStr)
                .sentBy(adminEmail)
                .recipientsCount(recipients.size())
                .build();

        BroadcastNotification saved = broadcastRepository.save(record);

        auditService.logAction("BROADCAST_NOTIFICATION", "Notification", saved.getId().toString(), adminEmail,
                "Broadcasted '" + saved.getTitle() + "' to " + saved.getRecipientsCount() + " customers via [" + channelsStr + "]");

        return BroadcastNotificationDto.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .message(saved.getMessage())
                .targetAudience(saved.getTargetAudience())
                .targetUserId(saved.getTargetUserId())
                .targetUserName(saved.getTargetUserName())
                .channels(saved.getChannels())
                .sentBy(saved.getSentBy())
                .recipientsCount(saved.getRecipientsCount())
                .createdAt(saved.getCreatedAt())
                .whatsAppLinks(whatsAppLinks)
                .build();
    }

    @Override
    public List<BroadcastNotificationDto> getBroadcastHistory() {
        return broadcastRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BroadcastNotificationDto> getMyNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }

        if (user.getRole() == RoleType.ROLE_ADMIN) {
            return getBroadcastHistory();
        }

        return broadcastRepository.findByTargetAudienceOrTargetUserIdOrderByCreatedAtDesc("ALL", user.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private BroadcastNotificationDto mapToDto(BroadcastNotification n) {
        return BroadcastNotificationDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .targetAudience(n.getTargetAudience())
                .targetUserId(n.getTargetUserId())
                .targetUserName(n.getTargetUserName())
                .channels(n.getChannels())
                .sentBy(n.getSentBy())
                .recipientsCount(n.getRecipientsCount())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private String buildHtmlEmail(String title, String messageText, User user) {
        return "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;\">"
                + "<div style=\"background: linear-gradient(135deg, #059669, #0f766e); padding: 18px 20px; border-radius: 8px; color: #ffffff; text-align: center;\">"
                + "<h2 style=\"margin: 0; font-size: 20px;\">📢 Tiffin Service Announcement</h2>"
                + "</div>"
                + "<p style=\"font-size: 15px; color: #334155; margin-top: 20px;\">Hello <b>" + user.getFullName() + "</b>,</p>"
                + "<div style=\"background: #f8fafc; border-left: 4px solid #059669; padding: 18px; border-radius: 6px; margin: 20px 0;\">"
                + "<h3 style=\"margin: 0 0 10px 0; color: #0f172a; font-size: 16px;\">" + title + "</h3>"
                + "<p style=\"margin: 0; color: #334155; line-height: 1.6; white-space: pre-line;\">" + messageText + "</p>"
                + "</div>"
                + "<p style=\"font-size: 12px; color: #94a3b8; text-align: center;\">Tiffin Management System &bull; Official Notification</p>"
                + "</div>";
    }
}