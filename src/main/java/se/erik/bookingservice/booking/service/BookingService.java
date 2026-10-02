package se.erik.bookingservice.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepo;
    private final RoomService roomService;
    private final CustomerClient customerClient;

    public BookingService(BookingRepository bookingRepo, RoomService roomService,  CustomerClient customerClient) {
        this.bookingRepo = bookingRepo;
        this.roomService = roomService;
        this.customerClient = customerClient;
    }

    public List<Booking> getAllBookings(){
        log.info("Fetching all bookings");
        return bookingRepo.findAll();
    }

    public Booking getBookingById(Long id){
        log.info("Fetching booking {}", id);
        return bookingRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Booking {} not found", id);
                    return new NotFoundException("Booking with id " + id + " not found");
                });
    }

    public List<Booking> getBookingsForCustomer(Long customerId) {
        log.info("Fetching bookings for customer");

        LocalDate today = LocalDate.now();
        return bookingRepo.findByCustomerIdAndStartDateAfterAndStatus(
                customerId,
                today.minusDays(1),
                BookingStatus.ACTIVE
        );
    }

    public List<Booking> getBookingsForRoom(Long roomId) {
        log.info("Fetching bookings for room {}", roomId);
        return bookingRepo.findByRoomId(roomId);
    }

    @Transactional
    public Booking createBooking(CreateBookingRequest request, Long customerId, String authorizationHeader) {

        log.info("Creating booking in room {} from {} to {}",
                request.roomId(), request.startDate(), request.endDate());

        if(request.startDate().isAfter(request.endDate())){
            log.warn("Invalid date range: start {} is after end {}",
                    request.startDate(), request.endDate());
            throw new BadRequest("Start date cannot be after end date");
        }

        customerClient.getCustomerById(customerId,authorizationHeader);

        Room room = roomService.getRoomById(request.roomId());


        if (request.extraBed()) {
            if (room.getType() != RoomType.DOUBLE) {
                log.warn("Extra bed requested for non-double room {}", room.getId());
                throw new BadRequest("Extra beds only available for double rooms");
            }
            if (!room.isExtraBedAllowed()) {
                log.warn("Extra bed not allowed for room {}", room.getId());
                throw new BadRequest("Extra bed is not available for this room");
            }
        }

        if (isRoomBooked(room.getId(), request.startDate(), request.endDate())) {
            log.warn("Room {} is already booked between {} and {}",
                    room.getId(),request.startDate(), request.endDate());
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
        Booking saved = bookingRepo.save(booking);

        log.info("Booking {} created successfully", saved.getId());

        return saved;
    }

    @Transactional
    public Booking updateBooking(Long id, UpdateBookingRequest request, Long customerId) {

        log.info("Updating booking {}", id);

        Booking booking = getBookingById(id);

        if(request.startDate().isAfter(request.endDate())){
            log.warn("Invalid date range for booking {}: start {} is after end {}",
                    id, request.startDate(), request.endDate());
            throw new BadRequest("Start date cannot be after end date");
        }

        Room room = booking.getRoom();

        if(request.roomId() != null && !request.roomId().equals(booking.getRoom().getId())) {
            log.info("Booking {} changing room from {} to {}",
                    id,room.getId(),request.roomId());
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
            log.warn("Room {} is already booked during update period for booking {}", room.getId(), id);
            throw new ConflictException("Room is already booked between this period");
        }

        booking.setRoom(room);
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());

        Booking saved = bookingRepo.save(booking);
        log.info("Booking {} updated successfully", saved.getId());

        return saved;
    }

    public boolean isRoomBooked(Long roomId, LocalDate start, LocalDate end) {
        log.info("Checking if room {} is booked between {} and {}", roomId, start, end);
        return bookingRepo.existsByRoomIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                roomId,
                BookingStatus.ACTIVE,
                end,
                start
        );
    }

    public List<Room> getAvailableRoomsByDate(LocalDate date) {
        log.info("Fetching available rooms for date {}", date);
        return roomService.getAllRooms().stream()
                .filter(room -> !isRoomBooked(room.getId(), date, date))
                .toList();
    }

    public List<Room> getAvailableRoomsByInterval(LocalDate start, LocalDate end) {
        log.info("Fetching available rooms between {} and {}", start, end);
        return roomService.getAllRooms().stream()
                .filter(room -> !isRoomBooked(room.getId(), start, end))
                .toList();
    }

    public boolean hasActiveBookings(long customerId) {
        log.info("Checking active bookings for customer");
        return bookingRepo.existsByCustomerIdAndStatus(customerId, BookingStatus.ACTIVE);
    }

    public boolean roomHasActiveBookings(Long roomId) {
        log.info("Checking active bookings for room {}", roomId);
        return bookingRepo.existsByRoomIdAndStatus(roomId, BookingStatus.ACTIVE);
    }


    @Transactional
    public void cancelBooking(Long id, Long customerId){
        log.info("Cancelling booking {}", id);
        Booking booking = getBookingById(id);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            log.warn("Booking {} is already cancelled", id);
            throw new BadRequest("Booking with id " + id + " is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(booking);

        log.info("Booking {} cancelled successfully", id);
    }

    public boolean customerHasBooking(Long customerId, Long roomId) {
        log.info("Checking if customer has booking for room {}", roomId);
        return bookingRepo.existsByCustomerIdAndRoomIdAndStatus(customerId, roomId, BookingStatus.ACTIVE);
    }
}