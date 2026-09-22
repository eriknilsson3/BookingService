package se.erik.bookingservice.room.service;

import org.springframework.stereotype.Service;
import se.erik.bookingservice.error.BadRequest;
import se.erik.bookingservice.error.NotFoundException;
import se.erik.bookingservice.room.model.Room;
import se.erik.bookingservice.room.model.RoomType;
import se.erik.bookingservice.room.repository.RoomRepository;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getAllRooms(){
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id){
        return roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found"));
    }

    public Room createRoom(Room room){
        if(roomRepository.existsByRoomNumber(room.getRoomNumber())){
            throw new BadRequest("Room number already exists");
        }
        validateRoom(room);
        return roomRepository.save(room);
    }

    public Room updateRoom(long id, Room updatedRoom){
        Room room = getRoomById(id);
        validateRoom(updatedRoom);

        room.setRoomNumber(updatedRoom.getRoomNumber());
        room.setBeds(updatedRoom.getBeds());
        room.setPricePerNight(updatedRoom.getPricePerNight());
        room.setType(updatedRoom.getType());
        room.setExtraBedAllowed(updatedRoom.isExtraBedAllowed());
        return roomRepository.save(room);
    }

    private void validateRoom(Room room) {
        if (room.getType() == RoomType.SINGLE){
            if (room.isExtraBedAllowed()){
                throw new BadRequest("Single room cannot have extra beds");
            }
            if (room.getBeds() != 1){
                throw new BadRequest("Single rooms must have exactly 1 bed");
            }
        }
        if (room.getType() == RoomType.DOUBLE){
            if (room.getBeds() != 2){
                throw new BadRequest("Double rooms must have exactly 2 beds");
            }
        }
        if (room.getPricePerNight() < 1){
            throw new BadRequest("Price must be positiva");
        }
    }

    public void deleteRoom(Long id) {
        Room room = getRoomById(id);
        roomRepository.delete(room);
    }
}

