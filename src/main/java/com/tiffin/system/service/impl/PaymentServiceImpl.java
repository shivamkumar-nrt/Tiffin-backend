package com.tiffin.system.service.impl;

import com.tiffin.system.dto.CreatePaymentRequest;
import com.tiffin.system.dto.PaymentDto;
import com.tiffin.system.entity.*;
import com.tiffin.system.entity.enums.PaymentStatus;
import com.tiffin.system.entity.enums.RecordStatus;
import com.tiffin.system.exception.BadRequestException;
import com.tiffin.system.exception.ResourceNotFoundException;
import com.tiffin.system.repository.*;
import com.tiffin.system.service.AuditService;
import com.tiffin.system.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final TiffinRecordRepository tiffinRecordRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuditService auditService;

    private static final DateTimeFormatter NUM_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional
    public PaymentDto recordPayment(CreatePaymentRequest request, String creatorEmail) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        String paymentNumber = "PAY-" + LocalDate.now().format(NUM_DATE_FMT) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Payment payment = Payment.builder()
                .paymentNumber(paymentNumber)
                .user(user)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionRef(request.getTransactionRef())
                .notes(request.getNotes())
                .status(request.isMarkAsSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.PENDING_VERIFICATION)
                .verifiedBy(request.isMarkAsSuccess() ? creatorEmail : null)
                .verifiedAt(request.isMarkAsSuccess() ? LocalDateTime.now() : null)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        if (savedPayment.getStatus() == PaymentStatus.SUCCESS) {
            settleRecordsAndCreateInvoice(savedPayment, creatorEmail);
        }

        auditService.logAction("RECORD_PAYMENT", "Payment", savedPayment.getId().toString(), creatorEmail,
                "Recorded payment of Rs. " + savedPayment.getAmount() + " for " + user.getEmail() + " (" + savedPayment.getStatus() + ")");

        return mapToDto(savedPayment);
    }

    @Override
    @Transactional
    public PaymentDto markPaymentSuccess(Long paymentId, String adminEmail) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException("Payment is already verified and marked as SUCCESS");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setVerifiedBy(adminEmail);
        payment.setVerifiedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        settleRecordsAndCreateInvoice(savedPayment, adminEmail);

        auditService.logAction("VERIFY_PAYMENT", "Payment", paymentId.toString(), adminEmail,
                "Verified and confirmed payment " + payment.getPaymentNumber() + " of Rs. " + payment.getAmount());

        return mapToDto(savedPayment);
    }

    private void settleRecordsAndCreateInvoice(Payment payment, String actor) {
        User user = payment.getUser();
        List<TiffinRecord> unpaidRecords = tiffinRecordRepository.findByUserIdAndStatus(user.getId(), RecordStatus.UNPAID)
                .stream()
                .sorted(Comparator.comparing(TiffinRecord::getServiceDate))
                .collect(Collectors.toList());

        List<TiffinRecord> settledRecords = new ArrayList<>();
        BigDecimal remainingAmount = payment.getAmount();
        BigDecimal totalSettledAmount = BigDecimal.ZERO;

        for (TiffinRecord record : unpaidRecords) {
            record.setStatus(RecordStatus.BILLED_PAID);
            record.setPayment(payment);
            tiffinRecordRepository.save(record);
            settledRecords.add(record);
            totalSettledAmount = totalSettledAmount.add(record.getChargedAmount());
        }

        // If no prior records, but payment recorded, use today
        LocalDate startDate = settledRecords.isEmpty() ? LocalDate.now() : settledRecords.get(0).getServiceDate();
        LocalDate endDate = settledRecords.isEmpty() ? LocalDate.now() : settledRecords.get(settledRecords.size() - 1).getServiceDate();

        String invoiceNumber = "INV-" + LocalDate.now().format(NUM_DATE_FMT) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .user(user)
                .payment(payment)
                .billingStartDate(startDate)
                .billingEndDate(endDate)
                .totalAmount(totalSettledAmount.compareTo(BigDecimal.ZERO) > 0 ? totalSettledAmount : payment.getAmount())
                .paymentAmount(payment.getAmount())
                .paymentStatus(PaymentStatus.SUCCESS)
                .notes("Settlement for payment #" + payment.getPaymentNumber())
                .generatedBy(actor)
                .build();

        for (TiffinRecord rec : settledRecords) {
            InvoiceItem item = InvoiceItem.builder()
                    .tiffinRecord(rec)
                    .serviceDate(rec.getServiceDate())
                    .tiffinType(rec.getTiffinType())
                    .menuSummary(rec.getMenuSnapshot())
                    .chargedAmount(rec.getChargedAmount())
                    .build();
            invoice.addItem(item);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);

        auditService.logAction("GENERATE_INVOICE", "Invoice", savedInvoice.getId().toString(), actor,
                "Generated invoice " + savedInvoice.getInvoiceNumber() + " with " + settledRecords.size() + " line items for " + user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> searchPayments(Long userId, PaymentStatus status, Pageable pageable) {
        return paymentRepository.searchPayments(userId, status, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> getMyPayments(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    private PaymentDto mapToDto(Payment pay) {
        Invoice invoice = invoiceRepository.findAll().stream()
                .filter(inv -> inv.getPayment() != null && inv.getPayment().getId().equals(pay.getId()))
                .findFirst()
                .orElse(null);

        return PaymentDto.builder()
                .id(pay.getId())
                .paymentNumber(pay.getPaymentNumber())
                .userId(pay.getUser().getId())
                .userName(pay.getUser().getFullName())
                .userEmail(pay.getUser().getEmail())
                .amount(pay.getAmount())
                .paymentMethod(pay.getPaymentMethod())
                .transactionRef(pay.getTransactionRef())
                .notes(pay.getNotes())
                .status(pay.getStatus())
                .verifiedBy(pay.getVerifiedBy())
                .verifiedAt(pay.getVerifiedAt())
                .createdAt(pay.getCreatedAt())
                .invoiceId(invoice != null ? invoice.getId() : null)
                .invoiceNumber(invoice != null ? invoice.getInvoiceNumber() : null)
                .build();
    }
}
