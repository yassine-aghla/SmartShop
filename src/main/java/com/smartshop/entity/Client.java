package com.smartshop.entity;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerTier customerTier = CustomerTier.BASIC;


    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @Column(name = "total_orders", nullable = false)
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(name = "total_spent", nullable = false)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "first_order_date")
    private LocalDateTime firstOrderDate;

    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;




    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.customerTier == null) {
            this.customerTier = CustomerTier.BASIC;
        }
        if (this.totalSpent == null) {
            this.totalSpent = BigDecimal.ZERO;
        }
        if (this.totalOrders == null) {
            this.totalOrders = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public CustomerTier calculateTier() {
        if (this.totalOrders >= 20 || this.totalSpent.compareTo(new BigDecimal("15000")) >= 0) {
            return CustomerTier.PLATINUM;
        }
        if (this.totalOrders >= 10 || this.totalSpent.compareTo(new BigDecimal("5000")) >= 0) {
            return CustomerTier.GOLD;
        }
        if (this.totalOrders >= 3 || this.totalSpent.compareTo(new BigDecimal("1000")) >= 0) {
            return CustomerTier.SILVER;
        }
        return CustomerTier.BASIC;
    }


    public void updateTier() {
        this.customerTier = calculateTier();
    }


    public void updateStatisticsAfterOrder(BigDecimal orderAmount) {
        this.totalOrders = this.totalOrders + 1;
        this.totalSpent = this.totalSpent.add(orderAmount);

        LocalDateTime now = LocalDateTime.now();
        if (this.firstOrderDate == null) {
            this.firstOrderDate = now;
        }
        this.lastOrderDate = now;

        updateTier();
    }


    public BigDecimal getDiscountPercentage(BigDecimal sousTotal) {
        switch (this.customerTier) {
            case PLATINUM:
                if (sousTotal.compareTo(new BigDecimal("1200")) >= 0) {
                    return new BigDecimal("15");
                }
                break;
            case GOLD:
                if (sousTotal.compareTo(new BigDecimal("800")) >= 0) {
                    return new BigDecimal("10");
                }
                break;
            case SILVER:
                if (sousTotal.compareTo(new BigDecimal("500")) >= 0) {
                    return new BigDecimal("5");
                }
                break;
            default:
                break;
        }
        return BigDecimal.ZERO;
    }


}