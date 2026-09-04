package com.tiffin.system.service.impl;

import com.tiffin.system.dto.AuditLogDto;
import com.tiffin.system.entity.AuditLog;
import com.tiffin.system.repository.AuditLogRepository;
import com.tiffin.system.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void logAction(String action, String entityName, String entityId, String performedBy, String details) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .performedBy(performedBy != null ? performedBy : "SYSTEM")
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable).map(this::mapToDto);
    }

    private AuditLogDto mapToDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .performedBy(log.getPerformedBy())
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build();
    }
}
