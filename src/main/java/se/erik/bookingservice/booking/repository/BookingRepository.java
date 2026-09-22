package se.erik.bookingservice.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.erik.bookingservice.booking.model.Booking;
import se.erik.bookingservice.booking.model.BookingStatus;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking,Long> {

    // Finns det en aktiv bokning på detta rum som överlappar önskad bokning? En create, ingen update
    boolean existsByRoomIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long roomId,
            BookingStatus status,
            LocalDate endDate,
            LocalDate startDate
    );


    boolean existsByRoomIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
            Long roomId,
            BookingStatus status,
            LocalDate endDate,
            LocalDate startDate,
            Long id
    );

    boolean existsByRoomIdAndStatus(Long roomId, BookingStatus status);

    boolean existsByCustomerIdAndStatus(Long customerId, BookingStatus status);

    boolean existsByCustomerIdAndRoomIdAndStatus(Long customerId, Long roomId, BookingStatus status);


    List<Booking> findByRoomId(Long roomId);

    List<Booking> findByCustomerIdAndStartDateAfterAndStatus(
            Long customerId,
            LocalDate date,
            BookingStatus status
    );

}
