package com.tiffin.system.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.tiffin.system.entity.enums.ItemCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = true)
    @JsonBackReference
    private Menu menu;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ItemCategory category;

    @Column(length = 255)
    private String description;

    @Builder.Default
    private boolean isSpicy = false;

    @Builder.Default
    private boolean isSweet = false;

    @Builder.Default
    private boolean isAvailable = true;
}