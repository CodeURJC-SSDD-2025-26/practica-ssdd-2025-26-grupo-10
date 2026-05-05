package es.urjc.ecomostoles.backend.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El nombre comercial es obligatorio")
        String commercialName,

        @NotBlank(message = "El CIF es obligatorio")
        String taxId,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        String contactEmail,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
        String password,

        @NotBlank(message = "La dirección es obligatoria")
        String address,

        String phone,
        String industrialSector,
        String description
) {}
