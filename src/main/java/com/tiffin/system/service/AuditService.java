package com.tiffin.system.service;

import com.tiffin.system.dto.AuditLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditService {
    void logAction(String action, String entityName, String entityId, String performedBy, String details);
    List<AuditLogDto> getRecentLogs();
    Page<AuditLogDto> getLogs(Pageable pageable);
}
