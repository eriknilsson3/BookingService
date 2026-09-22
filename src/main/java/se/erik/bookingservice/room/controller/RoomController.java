package se.erik.bookingservice.room.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import se.erik.bookingservice.room.model.Room;
import se.erik.bookingservice.room.service.RoomService;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> getAllRooms(){
        return roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    public Room getRoomById(@PathVariable Long id){
        return roomService.getRoomById(id);
    }

    @Valid
    @PostMapping
    public Room createRoom(@RequestBody Room room){
        return roomService.createRoom(room);
    }

    @Valid
    @PutMapping("/{id}")
    public Room updateRoom(@PathVariable Long id, @RequestBody Room updateRoom){
        return  roomService.updateRoom(id, updateRoom);
    }
    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable Long id){
        roomService.deleteRoom(id);
    }
}
