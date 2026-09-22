package se.erik.bookingservice.booking.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import se.erik.bookingservice.room.model.Room;

import java.time.LocalDate;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Long customerId;

    @ManyToOne(optional = false)
    private Room room;

    @NotNull(message="Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;

    @NotNull(message="End date is required")
    @FutureOrPresent(message="End date cannot be in the past")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private boolean extraBed;

    public Booking() {

    }

    public Booking(
            Long customerId, Room room, LocalDate startDate, LocalDate endDate, BookingStatus status) {
        this.customerId = customerId;
        this.room = room;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.extraBed = false;
    }

    public Booking(
            Long customerId, Room room, LocalDate startDate, LocalDate endDate, BookingStatus status, boolean extraBed) {
        this.customerId = customerId;
        this.room = room;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.extraBed = extraBed;
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

    public @NotNull(message = "Start date is required") @FutureOrPresent(message = "Start date cannot be in the past") LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(@NotNull(message = "Start date is required") @FutureOrPresent(message = "Start date cannot be in the past") LocalDate startDate) {
        this.startDate = startDate;
    }

    public @NotNull(message = "End date is required") @FutureOrPresent(message = "End date cannot be in the past") LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(@NotNull(message = "End date is required") @FutureOrPresent(message = "End date cannot be in the past") LocalDate endDate) {
        this.endDate = endDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public boolean isExtraBed() {
        return extraBed;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
