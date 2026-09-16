package org.example.pensionat.booking.model;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateBookingRequest(

        @NotNull(message = "Kund id måste anges")
        Long customerId,

        @NotNull(message = "Rum id måste anges")
        Long roomId,

        @NotNull(message = "Startdatum måste anges")
        LocalDate startDate,

        @NotNull(message = "Slutdatum måste anges")
        LocalDate endDate,

        boolean extraBed
) {
}
