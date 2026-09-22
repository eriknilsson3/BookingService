package se.erik.bookingservice.booking.model;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateBookingRequest(

        @NotNull(message= "Room ID cannot be null")
        Long roomId,

        @NotNull(message=" Start date cannot be null")
        LocalDate startDate,

        @NotNull(message="End date cannot be null")
        LocalDate endDate,

        boolean extraBed
) {}
