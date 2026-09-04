package com.tiffin.system.controller;

import com.tiffin.system.dto.ApiResponse;
import com.tiffin.system.dto.InvoiceDto;
import com.tiffin.system.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<InvoiceDto>>> getInvoices(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<InvoiceDto> invoices = invoiceService.searchInvoices(userId, startDate, endDate, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(invoices));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<InvoiceDto>>> getMyInvoices(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.getMyInvoices(userDetails.getUsername())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceDto>> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.getInvoiceById(id)));
    }

    @GetMapping("/number/{invoiceNumber}")
    public ResponseEntity<ApiResponse<InvoiceDto>> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.getInvoiceByNumber(invoiceNumber)));
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> downloadInvoicePdf(@PathVariable Long id) {
        ByteArrayInputStream bis = invoiceService.generateInvoicePdf(id);
        InvoiceDto inv = invoiceService.getInvoiceById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=Invoice-" + inv.getInvoiceNumber() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}
