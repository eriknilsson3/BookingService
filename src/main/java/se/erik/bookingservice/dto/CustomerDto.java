package se.erik.bookingservice.dto;

public record CustomerDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {}

