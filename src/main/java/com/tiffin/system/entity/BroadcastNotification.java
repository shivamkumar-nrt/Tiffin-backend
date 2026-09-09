package com.tiffin.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "broadcast_notifications", indexes = {
    @Index(name = "idx_bcast_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false, length = 50)
    private String targetAudience; // "ALL", "WITH_DUES", "SINGLE"

    private Long targetUserId;

    @Column(length = 100)
    private String targetUserName;

    @Column(length = 100)
    private String channels; // e.g. "EMAIL,WHATSAPP,PUSH"

    @Column(length = 100)
    private String sentBy;

    private int recipientsCount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}