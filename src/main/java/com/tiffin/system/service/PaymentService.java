package com.tiffin.system.service;

import com.tiffin.system.dto.CreatePaymentRequest;
import com.tiffin.system.dto.InvoiceDto;
import com.tiffin.system.dto.PaymentDto;
import com.tiffin.system.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {
    PaymentDto recordPayment(CreatePaymentRequest request, String creatorEmail);
    PaymentDto markPaymentSuccess(Long paymentId, String adminEmail);
    Page<PaymentDto> searchPayments(Long userId, PaymentStatus status, Pageable pageable);
    List<PaymentDto> getMyPayments(String userEmail);
    PaymentDto getPaymentById(Long id);
}
