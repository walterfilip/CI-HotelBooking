package org.example.pensionat.booking.service;

import jakarta.transaction.Transactional;
import org.example.pensionat.booking.BookingStatus;
import org.example.pensionat.booking.model.Booking;
import org.example.pensionat.booking.model.CreateBookingRequest;
import org.example.pensionat.booking.repository.BookingRepository;
import org.example.pensionat.customer.client.CustomerClient;
import org.example.pensionat.error.BadRequestException;
import org.example.pensionat.error.ForbiddenException;
import org.example.pensionat.error.NotFoundException;
import org.example.pensionat.room.RoomType;
import org.example.pensionat.room.model.Room;
import org.example.pensionat.room.repository.RoomRepository;
import org.example.pensionat.utils.Validations;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;


import java.time.temporal.ChronoUnit;

import java.time.LocalDate;
import java.util.List;


@Service
public class BookingService {

    private static final int extra_bed_price_per_night = 200;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CustomerClient customerClient;


    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, CustomerClient customerClient) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.customerClient = customerClient;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingByCustomerId(long customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    public int getTotalPrice(Room room, LocalDate startDate, LocalDate endDate, boolean extraBed) {

        long numberOfNights = ChronoUnit.DAYS.between(startDate, endDate);
        int totalPrice = room.getPrice() * (int) numberOfNights;

        if (extraBed) {
            totalPrice += extra_bed_price_per_night * (int) numberOfNights;
        }
        return totalPrice;
    }

    @Transactional
    public Booking createBooking(CreateBookingRequest request) {
        customerClient.getCustomer(request.customerId());

        Room room = roomRepository.findById(request.roomId()).orElseThrow(() -> new NotFoundException("Rummet finns inte"));

        Validations.validateDateRange(request.startDate(), request.endDate());
        validateRoomAvailability(request.roomId(), request.startDate(), request.endDate(), null);
        validateExtraBed(room, request.extraBed());

        Booking booking = new Booking(
                request.customerId(),
                room,
                request.startDate(),
                request.endDate(),
                request.extraBed(),
                BookingStatus.ACTIVE
        );

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, Long customerId) {

        Booking booking = getBookingById(bookingId, customerId);

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    private void validateRoomAvailability(Long roomId, LocalDate start, LocalDate end, Long bookingIdToIgnore) {
        List<Booking> bookings = bookingRepository.findByRoom_IdAndStatus(roomId, BookingStatus.ACTIVE);

        for (Booking existingBooking : bookings) {
            if (bookingIdToIgnore != null && bookingIdToIgnore.equals(existingBooking.getId())) {
                continue;
            }

            boolean overlap = !start.isAfter(existingBooking.getEndDate()) && !end.isBefore(existingBooking.getStartDate());

            if (overlap) {
                throw new BadRequestException("Rummet är redan bokat under valt datum");
            }
        }
    }

    private void validateExtraBed(Room room, boolean extraBedRequested) {
        if (extraBedRequested && room.getRoomType() != RoomType.DOUBLE) {
            throw new BadRequestException("Detta rum stödjer inte extrasäng");
        }
    }

    @Transactional
    public Booking changeBookingDate(CreateBookingRequest request, Long bookingId, Long customerId) {
        Booking booking = getBookingById(bookingId, customerId);

        Validations.validateDateRange(request.startDate(), request.endDate());
        validateRoomAvailability(request.roomId(), request.startDate(), request.endDate(), bookingId);

        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());

        return bookingRepository.save(booking);
    }

    @Transactional
    public void updateExpiredBookings() {
        List<Booking> bookings = bookingRepository.findAll();

        for (Booking booking : bookings) {

            if (booking.getStatus() == BookingStatus.ACTIVE && booking.getEndDate().isBefore(LocalDate.now())) {
                booking.setStatus(BookingStatus.CANCELLED);
            }
        }

        bookingRepository.saveAll(bookings);
    }

    public Booking getBookingById(Long bookingId, Long customerId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking finns inte"));

        if (!booking.getCustomerId().equals(customerId)) {
            throw new ForbiddenException("Du har inte behörighet att hantera den här bokningen");
        }

        return booking;
    }

    public boolean checkIfCustomerHasActiveBookings(Long customerId) {
        boolean hasActiveBookings = bookingRepository
                .existsByCustomerIdAndStatus(customerId, BookingStatus.ACTIVE);

        if (hasActiveBookings) {
            return true;
        }
        List<Booking> bookings = bookingRepository.findByCustomerId(customerId);
        bookingRepository.deleteAll(bookings);
        return false;
    }
}
