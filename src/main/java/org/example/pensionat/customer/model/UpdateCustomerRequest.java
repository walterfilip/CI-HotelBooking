package org.example.pensionat.customer.model;

import jakarta.validation.constraints.NotBlank;

public record UpdateCustomerRequest(
        @NotBlank(message = "Förnamn måste anges")
        String firstName,

        @NotBlank(message = "Efternamn måste anges")
        String lastName,

        @NotBlank(message = "Telefonnummer måste anges")
        String phoneNumber,

        String newPassword,

        String currentPassword,

        boolean changePassword
) {
}
