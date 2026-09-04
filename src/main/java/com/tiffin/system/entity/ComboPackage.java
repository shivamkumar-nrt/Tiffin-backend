package com.tiffin.system.entity;

import com.tiffin.system.entity.enums.TiffinType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "combo_packages", indexes = {
    @Index(name = "idx_combo_active", columnList = "isActive")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // e.g. "Full Thali", "Half Thali", "Executive Combo"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TiffinType tiffinType = TiffinType.FULL; // FULL, HALF

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "combo_items", joinColumns = @JoinColumn(name = "combo_id"))
    @Column(name = "item_name")
    @Builder.Default
    private List<String> includedItems = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}