package com.tiffin.system.entity;

import com.tiffin.system.entity.enums.RequestStatus;
import com.tiffin.system.entity.enums.TiffinType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tiffin_requests", indexes = {
    @Index(name = "idx_req_user_date", columnList = "user_id, serviceDate"),
    @Index(name = "idx_req_status", columnList = "status"),
    @Index(name = "idx_req_service_date", columnList = "serviceDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiffinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TiffinType tiffinType;

    @Column(length = 500)
    private String specialInstructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Column(length = 100)
    private String reviewedBy;

    @Column(length = 500)
    private String rejectionReason;

    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
