package com.tiffin.system.controller;

import com.tiffin.system.dto.ApiResponse;
import com.tiffin.system.dto.CreatePaymentRequest;
import com.tiffin.system.dto.PaymentDto;
import com.tiffin.system.dto.SubmitPaymentRequest;
import com.tiffin.system.entity.enums.PaymentStatus;
import com.tiffin.system.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> recordPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PaymentDto payment = paymentService.recordPayment(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Payment recorded and processed successfully", payment));
    }

    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentDto>> submitCustomerPayment(
            @Valid @RequestBody SubmitPaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PaymentDto payment = paymentService.submitCustomerPayment(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Payment submitted successfully. Awaiting admin verification.", payment));
    }

    @PatchMapping("/{id}/success")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> markPaymentSuccess(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PaymentDto payment = paymentService.markPaymentSuccess(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Payment verified and invoices generated", payment));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> rejectPayment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String reason = body != null && body.containsKey("reason") ? body.get("reason") : "Payment details could not be verified";
        PaymentDto payment = paymentService.rejectPayment(id, reason, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Payment has been marked as rejected", payment));
    }

    @GetMapping("/reminder-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReminderStatus() {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getReminderStatus()));
    }

    @PostMapping("/send-reminders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendReminders(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.sendBulkPaymentReminders(userDetails.getUsername())));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getPayments(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PaymentDto> payments = paymentService.searchPayments(userId, status, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(payments));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getMyPayments(userDetails.getUsername())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getPaymentById(id)));
    }
}
