package com.smartshop.dtos;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;
    @NotBlank(message = "Le nom du produit ne peut pas être vide")
    @Size(min = 3, max = 150, message = "Le nom doit faire entre 3 et 150 caractères")
    private String nom;

    @NotNull(message = "Le prix ne peut pas être null")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @Digits(integer = 10, fraction = 2, message = "Le prix doit avoir maximum 2 décimales")
    private BigDecimal prix;

    @NotNull(message = "Le stock ne peut pas être null")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stock;

    @Builder.Default
    private Boolean deleted = false;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
