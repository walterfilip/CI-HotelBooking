package org.example.pensionat.booking.service;

import org.example.pensionat.booking.BookingStatus;
import org.example.pensionat.booking.model.Booking;
import org.example.pensionat.booking.model.CreateBookingRequest;
import org.example.pensionat.booking.repository.BookingRepository;
import org.example.pensionat.customer.client.CustomerClient;
import org.example.pensionat.error.BadRequestException;
import org.example.pensionat.room.RoomType;
import org.example.pensionat.room.model.Room;
import org.example.pensionat.room.repository.RoomRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private CustomerClient customerClient;
    @InjectMocks
    private BookingService bookingService;

    private Room room;
    private Booking futureBooking;
    private Booking expiredBooking;
    private Booking cancelledBooking;
    private List<Booking> bookings;

    @BeforeEach
    void setUp() {
        room = new Room(RoomType.DOUBLE,"1","test",1000);
        room.setId(2L);

        futureBooking = new Booking(
                1L,
                room,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(4),
                false,
                BookingStatus.ACTIVE
        );
        expiredBooking = new Booking(
                1L,
                room,
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(3),
                true,
                BookingStatus.ACTIVE
        );

        cancelledBooking = new Booking(
                1L,
                room,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(4),
                false,
                BookingStatus.CANCELLED
        );

        bookings = List.of(futureBooking, expiredBooking, cancelledBooking);
    }

    @Test
    void getTotalPrice() {
        LocalDate startDate = LocalDate.of(2025,9,16);
        LocalDate endDate = LocalDate.of(2025,9,17);

        int result = bookingService.getTotalPrice(room,startDate,endDate,false);

        assertEquals(1000,result);
    }
    @Test
    void getTotalPriceWithExtraBed() {
        LocalDate startDate = LocalDate.of(2025,9,16);
        LocalDate endDate = LocalDate.of(2025,9,17);

        int result = bookingService.getTotalPrice(room,startDate,endDate,true);

        assertEquals(1200,result);

    }

    @Test
    void shouldCancelExpiredBookings() {
        //Mockito override, när. findAll anropas returnerar vi våran egna mock lista
        when(bookingRepository.findAll()).thenReturn(bookings);
        bookingService.updateExpiredBookings();
        assertEquals(BookingStatus.CANCELLED, expiredBooking.getStatus());
        verify(bookingRepository).saveAll(bookings);
    }

    @Test
    void getAllBookings() {

        when(bookingRepository.findAll()).thenReturn(bookings);
        List<Booking> results = bookingService.getAllBookings();
        assertNotNull(results);
        assertEquals(3, results.size());
        assertThat(results, Matchers.contains(futureBooking, expiredBooking, cancelledBooking));

        verify(bookingRepository).findAll();
    }

    @Test
    void throwExceptionWhenDateOverlap() {

        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                2L,
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(3),
                false
        );
        when(roomRepository.findById(2L)).thenReturn(Optional.of(room));
        when(bookingRepository.findByRoom_IdAndStatus(
                2L,BookingStatus.ACTIVE)).thenReturn(List.of(futureBooking));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(request));

        verify(customerClient).getCustomer(1L);
        verify(roomRepository).findById(2L);
        verify(bookingRepository).findByRoom_IdAndStatus(2L, BookingStatus.ACTIVE);
    }
}