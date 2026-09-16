package org.example.pensionat.room.service;

import org.example.pensionat.error.NotFoundException;
import org.example.pensionat.room.model.Room;
import org.example.pensionat.room.repository.RoomRepository;
import org.example.pensionat.booking.BookingStatus;
import org.example.pensionat.booking.model.Booking;
import org.example.pensionat.booking.repository.BookingRepository;
import org.example.pensionat.room.RoomType;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository repository;
    private final BookingRepository bookingRepository;

    public RoomService(RoomRepository repository, BookingRepository bookingRepository) {
        this.repository = repository;
        this.bookingRepository = bookingRepository;
    }

    public List<Room> getAllRooms() {
        return repository.findAll();
    }

    public Room getRoomById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Kunde ej hitta rummet angivet"));
    }

    public List<Room> getAvailableRooms(RoomType roomType, LocalDate startDate, LocalDate endDate) {
        List<Room> rooms = repository.findByRoomType(roomType);

        return rooms.stream().filter(room -> isRoomAvailable(room, startDate, endDate)).toList();
    }

    private boolean isRoomAvailable(Room room, LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByRoom_IdAndStatus(
                room.getId(),
                BookingStatus.ACTIVE
        );

        for (Booking booking : bookings) {
            boolean dateOverlap =
                    !startDate.isAfter(booking.getEndDate()) &&
                            !endDate.isBefore(booking.getStartDate());
            if (dateOverlap) {
                return false;
            }
        }
        return true;
    }
}
