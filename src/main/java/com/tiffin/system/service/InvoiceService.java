package com.tiffin.system.service;

import com.tiffin.system.dto.InvoiceDto;
import com.tiffin.system.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceService {
    InvoiceDto generateInvoiceForPayment(Payment payment, String generatedBy);
    InvoiceDto getInvoiceById(Long id);
    InvoiceDto getInvoiceByNumber(String invoiceNumber);
    Page<InvoiceDto> searchInvoices(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<InvoiceDto> getMyInvoices(String userEmail);
    ByteArrayInputStream generateInvoicePdf(Long invoiceId);
}
