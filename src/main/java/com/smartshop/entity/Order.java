package com.smartshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_code_id")
    private PromoCode promoCode;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();


    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "sous_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal sousTotal = BigDecimal.ZERO;

    @Column(name = "remise_fidelite_pourcentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal remiseFidelitePourcentage = BigDecimal.ZERO;

    @Column(name = "remise_fidelite_montant", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal remiseFideliteMontant = BigDecimal.ZERO;


    @Column(name = "remise_promo_pourcentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal remisePromoPourcentage = BigDecimal.ZERO;


    @Column(name = "remise_promo_montant", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal remisePromoPourcentage_montant = BigDecimal.ZERO;

    @Column(name = "remise_totale", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal remiseTotale = BigDecimal.ZERO;

    @Column(name = "montant_ht", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantHT = BigDecimal.ZERO;

    @Column(name = "taux_tva", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxTVA = new BigDecimal("20.00");

    @Column(name = "montant_tva", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantTVA = BigDecimal.ZERO;


    @Column(name = "total_ttc", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalTTC = BigDecimal.ZERO;

    @Column(name = "montant_paye", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantPaye = BigDecimal.ZERO;

    @Column(name = "montant_restant", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantRestant = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus statut = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_tier_at_order")
    private CustomerTier clientTierAtOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(length = 500)
    private String notes;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.orderDate = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = OrderStatus.PENDING;
        }
        if (this.tauxTVA == null) {
            this.tauxTVA = new BigDecimal("20.00");
        }
        initializeDefaults();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private void initializeDefaults() {
        if (this.sousTotal == null) this.sousTotal = BigDecimal.ZERO;
        if (this.remiseFidelitePourcentage == null) this.remiseFidelitePourcentage = BigDecimal.ZERO;
        if (this.remiseFideliteMontant == null) this.remiseFideliteMontant = BigDecimal.ZERO;
        if (this.remisePromoPourcentage == null) this.remisePromoPourcentage = BigDecimal.ZERO;
        if (this.remisePromoPourcentage_montant == null) this.remisePromoPourcentage_montant = BigDecimal.ZERO;
        if (this.remiseTotale == null) this.remiseTotale = BigDecimal.ZERO;
        if (this.montantHT == null) this.montantHT = BigDecimal.ZERO;
        if (this.montantTVA == null) this.montantTVA = BigDecimal.ZERO;
        if (this.totalTTC == null) this.totalTTC = BigDecimal.ZERO;
        if (this.montantPaye == null) this.montantPaye = BigDecimal.ZERO;
        if (this.montantRestant == null) this.montantRestant = BigDecimal.ZERO;
        if (this.orderItems == null) this.orderItems = new ArrayList<>();
        if (this.payments == null) this.payments = new ArrayList<>();
    }

    public void addOrderItem(OrderItem item) {
        if (this.orderItems == null) {
            this.orderItems = new ArrayList<>();
        }
        this.orderItems.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        if (this.orderItems != null) {
            this.orderItems.remove(item);
            item.setOrder(null);
        }
    }


    public void addPayment(Payment payment) {
        if (this.payments == null) {
            this.payments = new ArrayList<>();
        }
        this.payments.add(payment);
        payment.setOrder(this);
    }

    public boolean isFullyPaid() {
        return this.montantRestant.compareTo(BigDecimal.ZERO) <= 0;
    }


    public boolean canBeConfirmed() {
        return this.statut == OrderStatus.PENDING && isFullyPaid();
    }


    public boolean canBeCanceled() {
        return this.statut == OrderStatus.PENDING;
    }

    public boolean isFinalStatus() {
        return this.statut == OrderStatus.CONFIRMED
                || this.statut == OrderStatus.CANCELED
                || this.statut == OrderStatus.REJECTED;
    }
}