package com.smartshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer discountPercentage = 5;


    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(name = "expires_at")
    private LocalDateTime expiresAt;


    @Column(name = "max_uses")
    private Integer maxUses;


    @Column(name = "uses_count", nullable = false)
    @Builder.Default
    private Integer usesCount = 0;


    @Column(length = 255)
    private String description;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
        if (this.discountPercentage == null) {
            this.discountPercentage = 5;
        }
        if (this.usesCount == null) {
            this.usesCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public boolean isValid() {
        if (!this.active) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (this.expiresAt != null && now.isAfter(this.expiresAt)) {
            return false;
        }

        if (this.maxUses != null && this.usesCount >= this.maxUses) {
            return false;
        }

        return true;
    }


    public void incrementUseCount() {
        this.usesCount++;
    }
}