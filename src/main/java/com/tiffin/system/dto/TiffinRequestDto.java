package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.RequestStatus;
import com.tiffin.system.entity.enums.TiffinType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TiffinRequestDto {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userDepartment;
    private LocalDate serviceDate;
    private TiffinType tiffinType;
    private Long comboId;
    private String comboName;
    private String specialInstructions;
    private RequestStatus status;
    private String reviewedBy;
    private String rejectionReason;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}