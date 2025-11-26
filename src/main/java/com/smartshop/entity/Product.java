package com.smartshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false)
    private BigDecimal prix;


    @Column(nullable = false)
    private Integer stock;


    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;


    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(length = 500)
    private String description;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.deleted == null) {
            this.deleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return !this.deleted;
    }

    public boolean hasEnoughStock(Integer quantite) {
        return this.stock >= quantite;
    }

    public void decrementStock(Integer quantite) {
        if (hasEnoughStock(quantite)) {
            this.stock -= quantite;
        }
    }

    public void incrementStock(Integer quantite) {
        this.stock += quantite;
    }
}
