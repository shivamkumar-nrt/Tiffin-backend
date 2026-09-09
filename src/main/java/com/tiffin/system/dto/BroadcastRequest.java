package com.tiffin.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastRequest {

    @NotBlank(message = "Title / Subject is required")
    private String title;

    @NotBlank(message = "Message content is required")
    private String message;

    private String targetAudience; // "ALL", "WITH_DUES", "SINGLE"
    private Long targetUserId;

    @Builder.Default
    private boolean sendEmail = true;

    @Builder.Default
    private boolean sendWhatsApp = true;

    @Builder.Default
    private boolean sendPush = true;
}