package com.tiffin.system.entity;

import com.tiffin.system.entity.enums.PaymentMethod;
import com.tiffin.system.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_pay_number", columnList = "paymentNumber", unique = true),
    @Index(name = "idx_pay_user", columnList = "user_id"),
    @Index(name = "idx_pay_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String paymentNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(length = 100)
    private String transactionRef;

    private java.time.LocalDate paymentDate;

    @Column(length = 100)
    private String paymentApp;

    @Column(length = 500)
    private String notes;

    @Column(length = 500)
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING_VERIFICATION;

    @Column(length = 100)
    private String verifiedBy;

    private LocalDateTime verifiedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
