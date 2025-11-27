package com.smartshop.dtos;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {

    private Long id;

    private Long orderId;

    @NotNull(message = "L'ID du produit est obligatoire")
    private Long productId;

    private String productNom;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au minimum 1")
    private Integer quantite;

    @DecimalMin(value = "0.01", message = "Le prix unitaire doit être supérieur à 0")
    private BigDecimal prixUnitaire;

    private BigDecimal totalLigne;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}