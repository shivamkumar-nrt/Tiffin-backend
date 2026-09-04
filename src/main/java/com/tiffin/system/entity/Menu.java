package com.tiffin.system.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menus", indexes = {
    @Index(name = "idx_menu_service_date", columnList = "serviceDate", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate serviceDate;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Builder.Default
    private boolean isSpecial = false;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Builder.Default
    private List<MenuItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addItem(MenuItem item) {
        items.add(item);
        item.setMenu(this);
    }

    public void removeItem(MenuItem item) {
        items.remove(item);
        item.setMenu(null);
    }
}
