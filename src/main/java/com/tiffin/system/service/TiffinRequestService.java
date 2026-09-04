package com.tiffin.system.service;

import com.tiffin.system.dto.CreateTiffinRequest;
import com.tiffin.system.dto.ReviewRequestDto;
import com.tiffin.system.dto.TiffinRequestDto;
import com.tiffin.system.entity.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TiffinRequestService {
    TiffinRequestDto createRequest(CreateTiffinRequest request, String currentUserEmail);
    TiffinRequestDto approveRequest(Long requestId, String adminEmail);
    TiffinRequestDto rejectRequest(Long requestId, ReviewRequestDto reviewDto, String adminEmail);
    TiffinRequestDto cancelRequest(Long requestId, String userEmail);
    Page<TiffinRequestDto> getRequests(Long userId, RequestStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<TiffinRequestDto> getMyRequests(String userEmail);
    TiffinRequestDto getRequestById(Long id);
}
