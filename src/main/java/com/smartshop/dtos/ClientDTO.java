package com.smartshop.dtos;
import com.smartshop.entity.CustomerTier;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDTO {

    private Long id;
    @NotBlank(message = "Le nom du client ne peut pas être vide")
    @Size(min = 3, max = 150, message = "Le nom doit contenir entre 3 et 150 caractères")
    private String nom;
    @NotBlank(message = "L'email ne peut pas être vide")
    @Email(message = "L'email doit être valide")
    private String email;
    @NotNull(message = "Le tier client ne peut pas être null")
    private CustomerTier customerTier;
    @Builder.Default
    private Integer totalOrders = 0;
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;
    private LocalDateTime firstOrderDate;
    private LocalDateTime lastOrderDate;
    @Builder.Default
    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private String userUsername;
}