package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitPaymentRequest {

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String paymentApp; // e.g. "Google Pay", "PhonePe", "Paytm", "BHIM UPI", "Net Banking", "Cash"

    @NotNull(message = "Transaction reference / UTR number is required")
    private String transactionRef;

    private LocalDate paymentDate; // Date on which payment was done

    private String notes;
}
