package com.smartshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "numero_paiement", nullable = false)
    private Integer numeroPaiement;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;


    @Enumerated(EnumType.STRING)
    @Column(name = "type_paiement", nullable = false)
    private PaymentType typePaiement;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus statut = PaymentStatus.EN_ATTENTE;


    @Column(name = "date_paiement", nullable = false)
    private LocalDateTime datePaiement;


    @Column(name = "date_encaissement")
    private LocalDateTime dateEncaissement;

    @Column(length = 100)
    private String reference;

    @Column(length = 100)
    private String banque;


    @Column(name = "date_echeance")
    private LocalDateTime dateEcheance;


    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static final BigDecimal LIMITE_ESPECES = new BigDecimal("20000.00");

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.datePaiement == null) {
            this.datePaiement = LocalDateTime.now();
        }
        if (this.statut == null) {
            // Les espèces sont directement encaissées
            if (this.typePaiement == PaymentType.ESPECES) {
                this.statut = PaymentStatus.ENCAISSÉ;
                this.dateEncaissement = LocalDateTime.now();
            } else {
                this.statut = PaymentStatus.EN_ATTENTE;
            }
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean respecteLimiteEspeces() {
        if (this.typePaiement == PaymentType.ESPECES) {
            return this.montant.compareTo(LIMITE_ESPECES) <= 0;
        }
        return true;
    }


    public boolean isEncaisse() {
        return this.statut == PaymentStatus.ENCAISSÉ;
    }


    public void encaisser() {
        this.statut = PaymentStatus.ENCAISSÉ;
        this.dateEncaissement = LocalDateTime.now();
    }

    public void rejeter() {
        this.statut = PaymentStatus.REJETÉ;
    }

    public void generateReference(Long orderId) {
        String prefix;
        switch (this.typePaiement) {
            case ESPECES:
                prefix = "RECU";
                break;
            case CHEQUE:
                prefix = "CHQ";
                break;
            case VIREMENT:
                prefix = "VIR";
                break;
            default:
                prefix = "PAY";
        }
        this.reference = String.format("%s-%d-%d-%d",
                prefix,
                orderId,
                this.numeroPaiement,
                System.currentTimeMillis() % 10000);
    }
}