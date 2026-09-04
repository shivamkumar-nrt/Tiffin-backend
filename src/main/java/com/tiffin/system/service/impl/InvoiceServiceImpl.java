package com.tiffin.system.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.tiffin.system.dto.InvoiceDto;
import com.tiffin.system.dto.InvoiceItemDto;
import com.tiffin.system.entity.*;
import com.tiffin.system.entity.enums.RoleType;
import com.tiffin.system.exception.ResourceNotFoundException;
import com.tiffin.system.repository.InvoiceRepository;
import com.tiffin.system.repository.UserRepository;
import com.tiffin.system.service.AuditService;
import com.tiffin.system.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Override
    @Transactional
    public InvoiceDto generateInvoiceForPayment(Payment payment, String generatedBy) {
        throw new UnsupportedOperationException("Invoices are generated via payment settlement workflow");
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with number: " + invoiceNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceDto> searchInvoices(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return invoiceRepository.searchInvoices(userId, startDate, endDate, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDto> getMyInvoices(String userEmail) {
        return invoiceRepository.findAll().stream()
                .filter(inv -> inv.getUser().getEmail().equalsIgnoreCase(userEmail))
                .sorted((a, b) -> b.getGeneratedAt().compareTo(a.getGeneratedAt()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateInvoicePdf(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        // Fetch Administrator contact details for invoice branding
        Optional<User> adminUserOpt = userRepository.findAll().stream()
                .filter(u -> u.getRole() == RoleType.ROLE_ADMIN)
                .findFirst();

        String adminEmail = adminUserOpt.map(User::getEmail).orElse("shivamstm01@gmail.com");
        String adminPhone = adminUserOpt.map(User::getPhone).orElse("+91 9876543210");

        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Colors
            Color primaryColor = new Color(234, 88, 12); // Orange
            Color headerBg = new Color(249, 250, 251);   // Gray 50
            Color textDark = new Color(31, 41, 55);      // Gray 800
            Color textMuted = new Color(107, 114, 128);  // Gray 500
            Color borderLight = new Color(229, 231, 235);// Gray 200

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, primaryColor);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, textDark);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, textDark);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, textDark);
            Font smallMuted = FontFactory.getFont(FontFactory.HELVETICA, 9, textMuted);

            // Header Section Table (Brand + Admin Contact + Invoice Metadata)
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60, 40});

            PdfPCell brandCell = new PdfPCell();
            brandCell.setBorder(Rectangle.NO_BORDER);
            brandCell.addElement(new Paragraph("TIFFIN SYSTEM", titleFont));
            brandCell.addElement(new Paragraph("Fresh & Delicious Daily Meals", smallMuted));
            brandCell.addElement(new Paragraph("Administrator: " + adminEmail, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, textDark)));
            if (adminPhone != null && !adminPhone.trim().isEmpty()) {
                brandCell.addElement(new Paragraph("Support Helpline: " + adminPhone, smallMuted));
            }
            headerTable.addCell(brandCell);

            PdfPCell metaCell = new PdfPCell();
            metaCell.setBorder(Rectangle.NO_BORDER);
            metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph invTitle = new Paragraph("TAX INVOICE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, textDark));
            invTitle.setAlignment(Element.ALIGN_RIGHT);
            metaCell.addElement(invTitle);

            Paragraph invNum = new Paragraph("Invoice #: " + invoice.getInvoiceNumber(), boldFont);
            invNum.setAlignment(Element.ALIGN_RIGHT);
            metaCell.addElement(invNum);

            Paragraph invDate = new Paragraph("Date: " + invoice.getGeneratedAt().format(DATE_FORMATTER), regularFont);
            invDate.setAlignment(Element.ALIGN_RIGHT);
            metaCell.addElement(invDate);

            Paragraph payStatus = new Paragraph("Status: PAID", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(22, 101, 52)));
            payStatus.setAlignment(Element.ALIGN_RIGHT);
            metaCell.addElement(payStatus);

            headerTable.addCell(metaCell);
            document.add(headerTable);

            // Divider Line
            document.add(new Paragraph(" "));
            LineSeparator separator = new LineSeparator();
            separator.setLineColor(borderLight);
            document.add(separator);
            document.add(new Paragraph(" "));

            // Billed To & Billing Period Section
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{50, 50});

            PdfPCell billToCell = new PdfPCell();
            billToCell.setBorder(Rectangle.NO_BORDER);
            billToCell.addElement(new Paragraph("BILLED TO (CUSTOMER):", headerFont));
            billToCell.addElement(new Paragraph(invoice.getUser().getFullName(), boldFont));
            billToCell.addElement(new Paragraph("Email: " + invoice.getUser().getEmail(), regularFont));
            if (invoice.getUser().getPhone() != null && !invoice.getUser().getPhone().trim().isEmpty()) {
                billToCell.addElement(new Paragraph("Mobile: " + invoice.getUser().getPhone(), regularFont));
            }
            if (invoice.getUser().getDepartment() != null && !invoice.getUser().getDepartment().trim().isEmpty()) {
                billToCell.addElement(new Paragraph("Dept: " + invoice.getUser().getDepartment(), regularFont));
            }
            infoTable.addCell(billToCell);

            PdfPCell periodCell = new PdfPCell();
            periodCell.setBorder(Rectangle.NO_BORDER);
            periodCell.addElement(new Paragraph("BILLING DETAILS:", headerFont));
            periodCell.addElement(new Paragraph("Billing Period: " + invoice.getBillingStartDate().format(DATE_FORMATTER) + " to " + invoice.getBillingEndDate().format(DATE_FORMATTER), regularFont));
            if (invoice.getPayment() != null) {
                periodCell.addElement(new Paragraph("Payment Ref: " + invoice.getPayment().getPaymentNumber(), regularFont));
                periodCell.addElement(new Paragraph("Payment Method: " + invoice.getPayment().getPaymentMethod(), regularFont));
            }
            infoTable.addCell(periodCell);

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // Line Items Table
            PdfPTable itemsTable = new PdfPTable(5);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{8, 20, 16, 40, 16});

            // Table Header
            String[] headers = {"#", "Date", "Tiffin Type", "Menu / Items Description", "Amount (Rs.)"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, textDark)));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(8);
                cell.setBorderColor(borderLight);
                if (h.contains("Amount")) {
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                }
                itemsTable.addCell(cell);
            }

            int index = 1;
            for (InvoiceItem item : invoice.getItems()) {
                PdfPCell numCell = new PdfPCell(new Phrase(String.valueOf(index++), regularFont));
                numCell.setPadding(6);
                numCell.setBorderColor(borderLight);
                itemsTable.addCell(numCell);

                PdfPCell dateCell = new PdfPCell(new Phrase(item.getServiceDate().format(DATE_FORMATTER), regularFont));
                dateCell.setPadding(6);
                dateCell.setBorderColor(borderLight);
                itemsTable.addCell(dateCell);

                PdfPCell typeCell = new PdfPCell(new Phrase(item.getTiffinType().name(), regularFont));
                typeCell.setPadding(6);
                typeCell.setBorderColor(borderLight);
                itemsTable.addCell(typeCell);

                PdfPCell descCell = new PdfPCell(new Phrase(item.getMenuSummary() != null ? item.getMenuSummary() : "Standard Meal", regularFont));
                descCell.setPadding(6);
                descCell.setBorderColor(borderLight);
                itemsTable.addCell(descCell);

                PdfPCell amountCell = new PdfPCell(new Phrase(item.getChargedAmount().setScale(2).toString(), regularFont));
                amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                amountCell.setPadding(6);
                amountCell.setBorderColor(borderLight);
                itemsTable.addCell(amountCell);
            }

            document.add(itemsTable);

            // Summary / Totals Table
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(45);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.setWidths(new float[]{50, 50});

            PdfPCell totalLabelCell = new PdfPCell(new Phrase("Total Billable Amount:", boldFont));
            totalLabelCell.setPadding(6);
            totalLabelCell.setBorder(Rectangle.NO_BORDER);
            summaryTable.addCell(totalLabelCell);

            PdfPCell totalValCell = new PdfPCell(new Phrase("Rs. " + invoice.getTotalAmount().setScale(2), boldFont));
            totalValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValCell.setPadding(6);
            totalValCell.setBorder(Rectangle.NO_BORDER);
            summaryTable.addCell(totalValCell);

            PdfPCell paidLabelCell = new PdfPCell(new Phrase("Amount Paid:", boldFont));
            paidLabelCell.setPadding(6);
            paidLabelCell.setBorder(Rectangle.NO_BORDER);
            summaryTable.addCell(paidLabelCell);

            PdfPCell paidValCell = new PdfPCell(new Phrase("Rs. " + invoice.getPaymentAmount().setScale(2), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(22, 101, 52))));
            paidValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            paidValCell.setPadding(6);
            paidValCell.setBorder(Rectangle.NO_BORDER);
            summaryTable.addCell(paidValCell);

            document.add(new Paragraph(" "));
            document.add(summaryTable);

            // Footer Note
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Thank you for using our Tiffin Service. This is a computer-generated tax invoice and requires no physical signature.", smallMuted);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private InvoiceDto mapToDto(Invoice inv) {
        List<InvoiceItemDto> itemDtos = inv.getItems().stream().map(i -> InvoiceItemDto.builder()
                .id(i.getId())
                .tiffinRecordId(i.getTiffinRecord().getId())
                .serviceDate(i.getServiceDate())
                .tiffinType(i.getTiffinType())
                .menuSummary(i.getMenuSummary())
                .chargedAmount(i.getChargedAmount())
                .build()
        ).collect(Collectors.toList());

        return InvoiceDto.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .userId(inv.getUser().getId())
                .userName(inv.getUser().getFullName())
                .userEmail(inv.getUser().getEmail())
                .userPhone(inv.getUser().getPhone())
                .userDepartment(inv.getUser().getDepartment())
                .paymentId(inv.getPayment() != null ? inv.getPayment().getId() : null)
                .paymentNumber(inv.getPayment() != null ? inv.getPayment().getPaymentNumber() : null)
                .billingStartDate(inv.getBillingStartDate())
                .billingEndDate(inv.getBillingEndDate())
                .totalAmount(inv.getTotalAmount())
                .paymentAmount(inv.getPaymentAmount())
                .paymentStatus(inv.getPaymentStatus())
                .notes(inv.getNotes())
                .generatedBy(inv.getGeneratedBy())
                .generatedAt(inv.getGeneratedAt())
                .items(itemDtos)
                .build();
    }
}