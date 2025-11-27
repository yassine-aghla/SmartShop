package com.smartshop.dtos;

import com.smartshop.entity.OrderStatus;
import jakarta.validation.constraints.*;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusDTO {

    @NotNull(message = "Le nouveau statut ne peut pas être null")
    private OrderStatus statut;

    @Size(max = 500, message = "Les remarques ne doivent pas dépasser 500 caractères")
    private String remarques;
}
