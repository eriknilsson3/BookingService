package se.erik.bookingservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.erik.bookingservice.room.model.Room;
import se.erik.bookingservice.room.model.RoomType;
import se.erik.bookingservice.room.repository.RoomRepository;

@Configuration
public class DatabaseDataInit {

    @Bean
    CommandLineRunner initRooms(RoomRepository roomRepository) {
        return args -> {
            if (roomRepository.count() <= 3) {
                roomRepository.save(new Room(
                        "22", RoomType.DOUBLE, false, 2, 800));
                roomRepository.save(new Room(
                        "11", RoomType.SINGLE, true, 1, 400));
                roomRepository.save(new Room(
                        "948", RoomType.DOUBLE, true, 2, 1200));
                roomRepository.save(new Room(
                        "108",  RoomType.SINGLE, false, 3, 799));
                roomRepository.save(new Room(
                        "109",  RoomType.DOUBLE, true, 4, 1250));
                roomRepository.save(new Room(
                        "110",  RoomType.SINGLE, false, 1, 250));
                roomRepository.save(new Room(
                        "111",  RoomType.DOUBLE, true, 5, 950));
                roomRepository.save(new Room(
                        "112",  RoomType.DOUBLE, false, 5, 350));
                roomRepository.save(new Room(
                        "113",  RoomType.SINGLE, true, 6, 1999));
                roomRepository.save(new Room(
                        "114",  RoomType.DOUBLE, true, 8, 2499));


                System.out.println("Created init. rooms... ");
            }
        };
    }
}
