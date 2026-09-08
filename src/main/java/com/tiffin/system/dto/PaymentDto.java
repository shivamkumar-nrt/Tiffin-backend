package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.PaymentMethod;
import com.tiffin.system.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private Long id;
    private String paymentNumber;
    private Long userId;
    private String userName;
    private String userEmail;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String paymentApp;
    private java.time.LocalDate paymentDate;
    private String transactionRef;
    private String notes;
    private String rejectionReason;
    private PaymentStatus status;
    private String verifiedBy;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private Long invoiceId;
    private String invoiceNumber;
}
