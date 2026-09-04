package com.tiffin.system.controller;

import com.tiffin.system.dto.ApiResponse;
import com.tiffin.system.dto.CreateTiffinRequest;
import com.tiffin.system.dto.ReviewRequestDto;
import com.tiffin.system.dto.TiffinRequestDto;
import com.tiffin.system.entity.enums.RequestStatus;
import com.tiffin.system.service.TiffinRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tiffin-requests")
@RequiredArgsConstructor
public class TiffinRequestController {

    private final TiffinRequestService tiffinRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<TiffinRequestDto>> submitRequest(
            @Valid @RequestBody CreateTiffinRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TiffinRequestDto dto = tiffinRequestService.createRequest(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Tiffin request submitted successfully", dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<TiffinRequestDto>>> getRequests(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TiffinRequestDto> requests = tiffinRequestService.getRequests(userId, status, startDate, endDate, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(requests));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TiffinRequestDto>>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(tiffinRequestService.getMyRequests(userDetails.getUsername())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TiffinRequestDto>> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(tiffinRequestService.getRequestById(id)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TiffinRequestDto>> approveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        TiffinRequestDto approved = tiffinRequestService.approveRequest(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Request approved & consumption record generated", approved));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TiffinRequestDto>> rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewRequestDto reviewDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        TiffinRequestDto rejected = tiffinRequestService.rejectRequest(id, reviewDto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Request rejected", rejected));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<TiffinRequestDto>> cancelRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        TiffinRequestDto cancelled = tiffinRequestService.cancelRequest(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Request cancelled successfully", cancelled));
    }
}
