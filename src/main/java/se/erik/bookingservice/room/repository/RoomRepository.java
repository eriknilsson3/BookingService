package se.erik.bookingservice.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.erik.bookingservice.room.model.Room;

public interface RoomRepository extends JpaRepository<Room,Long> {
    boolean existsByRoomNumber(String roomNumber);
}
