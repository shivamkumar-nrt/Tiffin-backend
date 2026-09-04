package com.tiffin.system.controller;

import com.tiffin.system.dto.ApiResponse;
import com.tiffin.system.dto.TiffinRecordDto;
import com.tiffin.system.entity.enums.RecordStatus;
import com.tiffin.system.service.TiffinRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tiffin-records")
@RequiredArgsConstructor
public class TiffinRecordController {

    private final TiffinRecordService tiffinRecordService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<TiffinRecordDto>>> getRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) RecordStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TiffinRecordDto> records = tiffinRecordService.searchRecords(userId, status, startDate, endDate, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(records));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TiffinRecordDto>>> getMyRecords(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(tiffinRecordService.getMyRecords(userDetails.getUsername())));
    }

    @GetMapping("/by-date")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TiffinRecordDto>>> getRecordsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(tiffinRecordService.getRecordsByDate(date)));
    }

    @GetMapping("/unpaid/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TiffinRecordDto>>> getUnpaidRecords(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(tiffinRecordService.getUnpaidRecordsForUser(userId)));
    }
}
