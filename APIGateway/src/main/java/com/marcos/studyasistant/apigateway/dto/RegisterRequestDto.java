package com.marcos.studyasistant.apigateway.dto;

import jakarta.validation.constraints.*;

public record RegisterRequestDto(
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        String name,

        @Size(max = 50, message = "El apellido no puede exceder 50 caracteres")
        String surname,

        @NotBlank(message = "El email es requerido")
        @Email(message = "El formato del email no es válido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        String email,

        @NotBlank(message = "El username es requerido")
        @Size(min = 3, max = 30, message = "El username debe tener entre 3 y 30 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El username solo puede contener letras, números, puntos, guiones y guiones bajos")
        String username,

        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        String password,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "El formato del teléfono no es válido")
        String phone,

        @NotBlank(message = "El rol es requerido")
        @Pattern(regexp = "^(ROLE_USER|ROLE_ADMIN)$", message = "El rol debe ser USER o ADMIN")
        String role
) {}