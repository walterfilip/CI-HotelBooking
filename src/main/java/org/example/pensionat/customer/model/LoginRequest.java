package org.example.pensionat.customer.model;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "E-post måste anges")
        String email,

        @NotBlank(message = "Lösenord måste anges")
        String password
) {
}