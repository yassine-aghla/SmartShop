package com.smartshop.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderDTO {

    @NotNull(message = "L'ID du client est obligatoire")
    private Long clientId;

    @NotEmpty(message = "La commande doit contenir au moins un article")
    @Valid
    private List<CreateOrderItemDTO> items;

    @Pattern(regexp = "^(PROMO-[A-Z0-9]{4})?$", message = "Le code promo doit avoir le format PROMO-XXXX")
    private String promoCode;

    private String notes;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateOrderItemDTO {

        @NotNull(message = "L'ID du produit est obligatoire")
        private Long productId;

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité doit être au minimum 1")
        private Integer quantite;
    }
}