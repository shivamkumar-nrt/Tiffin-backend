package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDto {
    private Long id;
    private String invoiceNumber;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String userDepartment;
    private Long paymentId;
    private String paymentNumber;
    private LocalDate billingStartDate;
    private LocalDate billingEndDate;
    private BigDecimal totalAmount;
    private BigDecimal paymentAmount;
    private PaymentStatus paymentStatus;
    private String notes;
    private String generatedBy;
    private LocalDateTime generatedAt;

    @Builder.Default
    private List<InvoiceItemDto> items = new ArrayList<>();
}
