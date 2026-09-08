package com.tiffin.system.service;

import com.tiffin.system.dto.CreatePaymentRequest;
import com.tiffin.system.dto.PaymentDto;
import com.tiffin.system.dto.SubmitPaymentRequest;
import com.tiffin.system.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PaymentService {
    PaymentDto recordPayment(CreatePaymentRequest request, String creatorEmail);
    PaymentDto submitCustomerPayment(SubmitPaymentRequest request, String userEmail);
    PaymentDto markPaymentSuccess(Long paymentId, String adminEmail);
    PaymentDto rejectPayment(Long paymentId, String rejectionReason, String adminEmail);
    Page<PaymentDto> searchPayments(Long userId, PaymentStatus status, Pageable pageable);
    List<PaymentDto> getMyPayments(String userEmail);
    PaymentDto getPaymentById(Long id);
    Map<String, Object> getReminderStatus();
    Map<String, Object> sendBulkPaymentReminders(String triggerSource);
}
