package com.smartshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;


@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;


    @Column(nullable = false)
    private Integer quantite;


    @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "total_ligne", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalLigne;


    @Column(name = "product_nom", nullable = false, length = 150)
    private String productNom;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculateTotalLigne();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateTotalLigne();
    }

    public void calculateTotalLigne() {
        if (this.quantite != null && this.prixUnitaire != null) {
            this.totalLigne = this.prixUnitaire
                    .multiply(BigDecimal.valueOf(this.quantite))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.totalLigne = BigDecimal.ZERO;
        }
    }

    public void initFromProduct(Product product, Integer quantite) {
        this.product = product;
        this.quantite = quantite;
        this.prixUnitaire = product.getPrix();
        this.productNom = product.getNom();
        calculateTotalLigne();
    }
}