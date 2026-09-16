package org.example.pensionat.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.example.pensionat.booking.BookingStatus;
import org.example.pensionat.room.model.Room;

import java.time.LocalDate;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private Long customerId;

    @ManyToOne(optional = false)
    private Room room;

    @NotNull(message = "Start datum måste anges")
    private LocalDate startDate;

    @NotNull(message = "Slut datum måste anges")
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean extraBed = false;

    @NotNull(message = "En bokning måste ha en status")
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    public Booking() {
    }

    public Booking(Long customerId, Room room, LocalDate startDate, LocalDate endDate, boolean extraBed, BookingStatus status) {
        this.customerId = customerId;
        this.room = room;
        this.startDate = startDate;
        this.endDate = endDate;
        this.extraBed = extraBed;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isExtraBed() {
        return extraBed;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public boolean datePassed() {
        return startDate.isBefore(LocalDate.now());
    }
}