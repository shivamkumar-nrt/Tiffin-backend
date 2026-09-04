package com.tiffin.system.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.tiffin.system.entity.enums.TiffinType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonBackReference
    private Invoice invoice;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tiffin_record_id", nullable = false)
    private TiffinRecord tiffinRecord;

    @Column(nullable = false)
    private LocalDate serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TiffinType tiffinType;

    @Column(length = 500)
    private String menuSummary;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal chargedAmount;
}
