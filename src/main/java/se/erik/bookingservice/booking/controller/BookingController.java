package se.erik.bookingservice.booking.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import se.erik.bookingservice.booking.model.Booking;
import se.erik.bookingservice.booking.model.CreateBookingRequest;
import se.erik.bookingservice.booking.model.UpdateBookingRequest;
import se.erik.bookingservice.booking.service.BookingService;
import se.erik.bookingservice.room.model.Room;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<Booking> getAllBookings(){
        return bookingService.getAllBookings();
    }

    @GetMapping("/{id}")
    public Booking getBookingById(@PathVariable Long id){
        return bookingService.getBookingById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<Booking> getBookingsForCustomer(@PathVariable Long customerId){
        return bookingService.getBookingsForCustomer(customerId);
    }

    @GetMapping("/customer/{customerId}/active")
    public boolean hasActiveBookings(@PathVariable Long customerId){
        return bookingService.hasActiveBookings(customerId);
    }

    @GetMapping("/room/{roomId}")
    public List<Booking>getBookingsForRoom(@PathVariable Long roomId){
        return bookingService.getBookingsForRoom(roomId);
    }

    @GetMapping("/available")
    public List<Room> getAvailableRoomsByDate(@RequestParam LocalDate date) {
        return bookingService.getAvailableRoomsByDate(date);
    }

    @GetMapping("/my")
    public List<Booking> getMyBookings(Authentication authentication){
        Long customerId = (Long) authentication.getPrincipal();
        return bookingService.getBookingsForCustomer(customerId);
    }

    @GetMapping("/available-range")
    public List<Room> getAvailableRoomsByInterval(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        return bookingService.getAvailableRoomsByInterval(start, end);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(@Valid @RequestBody CreateBookingRequest request, Authentication authentication,
                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        Long customerId = (Long) authentication.getPrincipal();
        return bookingService.createBooking(request, customerId, authorizationHeader);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable Long id, Authentication authentication){
        Long customerId = (Long) authentication.getPrincipal();
        bookingService.cancelBooking(id, customerId);
    }

    @PutMapping("/{id}")
    public Booking updateBooking(@PathVariable Long id,
                                 @Valid @RequestBody UpdateBookingRequest request, Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        return bookingService.updateBooking(id, request, customerId);
    }

    @GetMapping("/customer/{customerId}/room/{roomId}")
    public boolean customerHasBooking(@PathVariable Long customerId, @PathVariable Long roomId){
        return bookingService.customerHasBooking(customerId,roomId);
    }
}
