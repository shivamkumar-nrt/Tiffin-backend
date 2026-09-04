package com.tiffin.system.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.tiffin.system.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_inv_number", columnList = "invoiceNumber", unique = true),
    @Index(name = "idx_inv_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(nullable = false)
    private LocalDate billingStartDate;

    @Column(nullable = false)
    private LocalDate billingEndDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.SUCCESS;

    @Column(length = 500)
    private String notes;

    @Column(length = 100)
    private String generatedBy;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
    }
}
