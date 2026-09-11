package com.tiffin.system.service;

import com.tiffin.system.dto.TiffinRecordDto;
import com.tiffin.system.entity.enums.RecordStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TiffinRecordService {
    Page<TiffinRecordDto> searchRecords(Long userId, RecordStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<TiffinRecordDto> getMyRecords(String userEmail);
    List<TiffinRecordDto> getRecordsByDate(LocalDate date);
    List<TiffinRecordDto> getUnpaidRecordsForUser(Long userId);
    TiffinRecordDto addManualRecord(com.tiffin.system.dto.ManualTiffinRecordRequest request, String adminEmail);
}
