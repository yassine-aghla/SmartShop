package com.smartshop.dtos;

import com.smartshop.entity.PaymentStatus;
import com.smartshop.entity.PaymentType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {

    private Long id;

    @NotNull(message = "L'ID de la commande est obligatoire")
    private Long orderId;

    private String orderReference;

    private Integer numeroPaiement;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    @Digits(integer = 10, fraction = 2, message = "Le montant doit avoir maximum 2 décimales")
    private BigDecimal montant;

    @NotNull(message = "Le type de paiement est obligatoire")
    private PaymentType typePaiement;

    private PaymentStatus statut;

    private LocalDateTime datePaiement;

    private LocalDateTime dateEncaissement;

    private String reference;

    @Size(max = 100, message = "Le nom de la banque ne doit pas dépasser 100 caractères")
    private String banque;

    private LocalDateTime dateEcheance;

    @Size(max = 500, message = "Les notes ne doivent pas dépasser 500 caractères")
    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private BigDecimal montantRestantApres;

    private Boolean isEncaisse;
}