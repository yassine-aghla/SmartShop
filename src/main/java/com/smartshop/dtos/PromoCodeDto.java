package com.smartshop.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCodeDto {

    private Long id;

    @NotBlank(message = "Le code promo ne peut pas être vide")
    @Pattern(
            regexp = "^PROMO-[A-Z0-9]{4}$",
            message = "Le code doit avoir le format PROMO-XXXX (X = lettres/chiffres majuscules)"
    )
    private String code;

    @Builder.Default
    private Boolean active = true;

    @NotNull(message = "Le pourcentage de remise ne peut pas être null")
    @Min(value = 1, message = "Le pourcentage doit être au minimum 1%")
    @Max(value = 100, message = "Le pourcentage doit être maximum 100%")
    private Integer discountPercentage;

    @Min(value = 0, message = "Le nombre d'utilisations ne peut pas être négatif")
    private Integer maxUses;

    @Builder.Default
    private Integer usesCount = 0;

    private LocalDateTime expiresAt;

    @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères")
    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}