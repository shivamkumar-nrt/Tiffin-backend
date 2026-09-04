package com.tiffin.system.controller;

import com.tiffin.system.dto.ApiResponse;
import com.tiffin.system.dto.CreatePaymentRequest;
import com.tiffin.system.dto.PaymentDto;
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

    @PatchMapping("/{id}/success")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> markPaymentSuccess(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PaymentDto payment = paymentService.markPaymentSuccess(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Payment verified and invoices generated", payment));
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
