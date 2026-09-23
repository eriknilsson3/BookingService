package se.erik.bookingservice.booking.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import se.erik.bookingservice.booking.client.CustomerClient;
import se.erik.bookingservice.booking.model.Booking;
import se.erik.bookingservice.booking.model.BookingStatus;
import se.erik.bookingservice.booking.model.CreateBookingRequest;
import se.erik.bookingservice.booking.model.UpdateBookingRequest;
import se.erik.bookingservice.booking.repository.BookingRepository;
import se.erik.bookingservice.error.BadRequest;
import se.erik.bookingservice.error.ConflictException;
import se.erik.bookingservice.error.NotFoundException;
import se.erik.bookingservice.room.model.Room;
import se.erik.bookingservice.room.model.RoomType;
import se.erik.bookingservice.room.service.RoomService;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepo;
    private final RoomService roomService;
    private final CustomerClient customerClient;

    public BookingService(BookingRepository bookingRepo, RoomService roomService,  CustomerClient customerClient) {
        this.bookingRepo = bookingRepo;
        this.roomService = roomService;
        this.customerClient = customerClient;
    }

    public List<Booking> getAllBookings(){
        return bookingRepo.findAll();
    }

    public Booking getBookingById(Long id){
        return bookingRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking with id " + id + " not found"));
    }

    public List<Booking> getBookingsForCustomer(Long customerId) {
        LocalDate today = LocalDate.now();
        return bookingRepo.findByCustomerIdAndStartDateAfterAndStatus(
                customerId,
                today.minusDays(1),
                BookingStatus.ACTIVE
        );
    }

    public List<Booking> getBookingsForRoom(Long roomId) {
        return bookingRepo.findByRoomId(roomId);
    }

    @Transactional
    public Booking createBooking(CreateBookingRequest request, Long customerId, String authorizationHeader) {

        if(request.startDate().isAfter(request.endDate())){
            throw new BadRequest("Start date cannot be after end date");
        }

        customerClient.getCustomerById(customerId,authorizationHeader);

        Room room = roomService.getRoomById(request.roomId());

        if (request.extraBed()) {
            if (room.getType() != RoomType.DOUBLE) {
                throw new BadRequest("Extra beds only available for double rooms");
            }
            if (!room.isExtraBedAllowed()) {
                throw new BadRequest("Extra bed is not available for this room");
            }
        }

        if (isRoomBooked(room.getId(), request.startDate(), request.endDate())) {
            throw new ConflictException("Room is already booked between " + request.startDate() +  " and " + request.endDate());
        }

        Booking booking = new Booking(
                customerId,
                room,
                request.startDate(),
                request.endDate(),
                BookingStatus.ACTIVE,
                request.extraBed()
        );
        return bookingRepo.save(booking);
    }

    @Transactional
    public Booking updateBooking(Long id, UpdateBookingRequest request, Long customerId) {

        Booking booking = getBookingById(id);

        if(request.startDate().isAfter(request.endDate())){
            throw new BadRequest("Start date cannot be after end date");
        }

        Room room = booking.getRoom();

        if(request.roomId() != null && !request.roomId().equals(booking.getRoom().getId())) {
            room = roomService.getRoomById(request.roomId());
        }

        boolean overlaps = bookingRepo. existsByRoomIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
                room.getId(),
                BookingStatus.ACTIVE,
                request.endDate(),
                request.startDate(),
                booking.getId()
        );
        if(overlaps) {
            throw new ConflictException("Room is already booked between this period");
        }

        booking.setRoom(room);
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());

        return bookingRepo.save(booking);
    }

    public boolean isRoomBooked(Long roomId, LocalDate start, LocalDate end) {
        return bookingRepo.existsByRoomIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                roomId,
                BookingStatus.ACTIVE,
                end,
                start
        );
    }

    public List<Room> getAvailableRoomsByDate(LocalDate date) {
        return roomService.getAllRooms().stream()
                .filter(room -> !isRoomBooked(room.getId(), date, date))
                .toList();
    }

    public List<Room> getAvailableRoomsByInterval(LocalDate start, LocalDate end) {
        return roomService.getAllRooms().stream()
                .filter(room -> !isRoomBooked(room.getId(), start, end))
                .toList();
    }

    public boolean hasActiveBookings(long customerId) {
        return bookingRepo.existsByCustomerIdAndStatus(customerId, BookingStatus.ACTIVE);
    }

    public boolean roomHasActiveBookings(Long roomId) {
        return bookingRepo.existsByRoomIdAndStatus(roomId, BookingStatus.ACTIVE);
    }


    @Transactional
    public void cancelBooking(Long id, Long customerId){
        Booking booking = getBookingById(id);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequest("Booking with id " + id + " is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(booking);
    }

    public boolean customerHasBooking(Long customerId, Long roomId) {
        return bookingRepo.existsByCustomerIdAndRoomIdAndStatus(customerId, roomId, BookingStatus.ACTIVE);
    }
}
