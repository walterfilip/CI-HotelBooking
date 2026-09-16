package org.example.pensionat.customer.model;

public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
}
