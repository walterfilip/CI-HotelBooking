package org.example.pensionat.booking.repository;

import org.example.pensionat.booking.BookingStatus;
import org.example.pensionat.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByRoom_IdAndStatus(Long roomId, BookingStatus status);

    List<Booking> findByCustomerId(long customerId);

    boolean existsByCustomerIdAndStatus(Long customerId, BookingStatus status);
}
