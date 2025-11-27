package com.smartshop.dtos;

import com.smartshop.entity.CustomerTier;
import com.smartshop.entity.OrderStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Long id;

    private String reference;

    @NotNull(message = "L'ID du client est obligatoire")
    private Long clientId;

    private String clientNom;

    private String clientEmail;

    private CustomerTier clientTier;

    @NotEmpty(message = "La commande doit contenir au moins un article")
    @Builder.Default
    private List<OrderItemDTO> orderItems = new ArrayList<>();

    private Long promoCodeId;

    private String promoCodeValue;

    private LocalDateTime orderDate;

    private BigDecimal sousTotal;

    private BigDecimal remiseFidelitePourcentage;

    private BigDecimal remiseFideliteMontant;

    private BigDecimal remisePromoPourcentage;

    private BigDecimal remisePromoMontant;

    private BigDecimal remiseTotale;

    private BigDecimal montantHT;

    private BigDecimal tauxTVA;

    private BigDecimal montantTVA;

    private BigDecimal totalTTC;

    private BigDecimal montantPaye;

    private BigDecimal montantRestant;

    private OrderStatus statut;

    private CustomerTier clientTierAtOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime canceledAt;

    private String notes;

    @Builder.Default
    private List<PaymentDTO> payments = new ArrayList<>();


    private Integer nombrePaiements;

    private Boolean isFullyPaid;

    private Boolean canBeConfirmed;
}