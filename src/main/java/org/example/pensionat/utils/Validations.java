package org.example.pensionat.utils;
import org.example.pensionat.error.BadRequestException;
import java.time.LocalDate;

public class Validations {

    public static void validateDateRange(LocalDate start, LocalDate end) {

        LocalDate today = LocalDate.now();
        if (start.isBefore(today) || end.isBefore(today)) {
            throw new BadRequestException("Du kan inte välja datum bakåt i tiden");

        }
        if (end.isBefore(start)) {
            throw new BadRequestException("Slutdatum måste vara efter startdatum");

        }
    }
}
