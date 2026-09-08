package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String paymentApp;
    private java.time.LocalDate paymentDate;
    private String transactionRef;
    private String notes;
    private boolean markAsSuccess = true; // For admin convenience
}
