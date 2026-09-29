package org.example.pensionat;

import org.example.pensionat.booking.service.BookingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PensionatApplication {

    public static void main(String[] args) {
        SpringApplication.run(PensionatApplication.class, args);
    }

    @Bean
    CommandLineRunner updateBoookings(BookingService bookingService) {
        return args -> bookingService.updateExpiredBookings();
    }
}
