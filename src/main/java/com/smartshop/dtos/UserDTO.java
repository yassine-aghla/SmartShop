package com.smartshop.dtos;

import com.smartshop.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    @NotBlank(message = "Le username ne peut pas être vide")
    @Size(min = 3, max = 100, message = "Le username doit contenir entre 3 et 100 caractères")
    private String username;

    @NotBlank(message = "Le password ne peut pas être vide")
    @Size(min = 6, message = "Le password doit contenir au minimum 6 caractères")
    private String password;

    @NotNull(message = "Le rôle ne peut pas être null")
    private UserRole role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}