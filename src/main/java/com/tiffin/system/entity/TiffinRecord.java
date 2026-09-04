package com.tiffin.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tiffin.system.entity.enums.RecordStatus;
import com.tiffin.system.entity.enums.TiffinType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tiffin_records", indexes = {
    @Index(name = "idx_rec_user_date", columnList = "user_id, serviceDate"),
    @Index(name = "idx_rec_status", columnList = "status"),
    @Index(name = "idx_rec_service_date", columnList = "serviceDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiffinRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private TiffinRequest request;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TiffinType tiffinType;

    @Column(length = 1000)
    private String menuSnapshot;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal chargedAmount; // Frozen historical rate

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RecordStatus status = RecordStatus.UNPAID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    @JsonIgnore
    private Payment payment;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
