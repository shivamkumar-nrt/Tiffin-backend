package com.tiffin.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogDto {
    private Long id;
    private String action;
    private String entityName;
    private String entityId;
    private String performedBy;
    private String details;
    private LocalDateTime timestamp;
}
