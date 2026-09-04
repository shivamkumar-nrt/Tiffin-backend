package com.tiffin.system.service.impl;

import com.tiffin.system.dto.TiffinRecordDto;
import com.tiffin.system.entity.TiffinRecord;
import com.tiffin.system.entity.User;
import com.tiffin.system.entity.enums.RecordStatus;
import com.tiffin.system.exception.ResourceNotFoundException;
import com.tiffin.system.repository.TiffinRecordRepository;
import com.tiffin.system.repository.UserRepository;
import com.tiffin.system.service.TiffinRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TiffinRecordServiceImpl implements TiffinRecordService {

    private final TiffinRecordRepository tiffinRecordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<TiffinRecordDto> searchRecords(Long userId, RecordStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return tiffinRecordRepository.searchRecords(userId, status, startDate, endDate, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TiffinRecordDto> getMyRecords(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        return tiffinRecordRepository.findByUserIdOrderByServiceDateDesc(user.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TiffinRecordDto> getRecordsByDate(LocalDate date) {
        return tiffinRecordRepository.findByServiceDateOrderByCreatedAtDesc(date).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TiffinRecordDto> getUnpaidRecordsForUser(Long userId) {
        return tiffinRecordRepository.findByUserIdAndStatus(userId, RecordStatus.UNPAID).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private TiffinRecordDto mapToDto(TiffinRecord rec) {
        return TiffinRecordDto.builder()
                .id(rec.getId())
                .requestId(rec.getRequest().getId())
                .userId(rec.getUser().getId())
                .userName(rec.getUser().getFullName())
                .userEmail(rec.getUser().getEmail())
                .serviceDate(rec.getServiceDate())
                .tiffinType(rec.getTiffinType())
                .menuSnapshot(rec.getMenuSnapshot())
                .chargedAmount(rec.getChargedAmount())
                .status(rec.getStatus())
                .paymentId(rec.getPayment() != null ? rec.getPayment().getId() : null)
                .createdAt(rec.getCreatedAt())
                .build();
    }
}
