package se.erik.bookingservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import se.erik.bookingservice.booking.client.CustomerClient;
import se.erik.bookingservice.booking.model.BookingStatus;
import se.erik.bookingservice.booking.repository.BookingRepository;
import se.erik.bookingservice.dto.CustomerDto;
import se.erik.bookingservice.room.model.Room;
import se.erik.bookingservice.room.model.RoomType;
import se.erik.bookingservice.room.repository.RoomRepository;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @MockitoBean
    private CustomerClient customerClient;

    private Room testRoom;

    @BeforeEach
    void setUp() {

        bookingRepository.deleteAll();

        testRoom = roomRepository.save(
                new Room(
                        "TEST-" + UUID.randomUUID(),
                        RoomType.SINGLE,
                        false,
                        1,
                        800
                )
        );

        testRoom = roomRepository.save(
                new Room(
                        "DOUBLE-TEST-"
                                + UUID.randomUUID(),
                        RoomType.DOUBLE,
                        true,
                        2,
                        1000
                )
        );

        when(customerClient.getCustomerById(
                eq(1L),
                anyString()
        )).thenReturn(
                new CustomerDto(
                        1L,
                        "Test",
                        "Customer",
                        "test@example.com",
                        "0700000000"
                )
        );

        when(customerClient.getCustomerById(
                eq(2L),
                anyString()
        )).thenReturn(
                new CustomerDto(
                        2L,
                        "Second",
                        "Customer",
                        "second@example.com",
                        "0700000001"
                )
        );
    }

    @Test
    void shouldCreateBookingSuccessfully() throws Exception {

        String bookingJson = """
            {
                "roomId": %d,
                "startDate": "2030-12-01",
                "endDate": "2030-12-05",
                "extraBed": false
            }
            """.formatted(testRoom.getId());

        mockMvc.perform(
                        post("/bookings")
                                .with(asCustomer(1L))
                                .header(
                                        "Authorization",
                                        "Bearer test-token"
                                )
                                .contentType("application/json")
                                .content(bookingJson)
                )
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        boolean bookingExists =
                bookingRepository
                        .existsByCustomerIdAndRoomIdAndStatus(
                                1L,
                                testRoom.getId(),
                                BookingStatus.ACTIVE
                        );

        org.junit.jupiter.api.Assertions.assertTrue(
                bookingExists
        );
    }

    @Test
    void shouldReturnConflictWhenRoomIsDoubleBooked()
            throws Exception {

        String firstBookingJson = """
            {
                "roomId": %d,
                "startDate": "2030-12-01",
                "endDate": "2030-12-05",
                "extraBed": false
            }
            """.formatted(testRoom.getId());

        mockMvc.perform(
                        post("/bookings")
                                .with(asCustomer(1L))
                                .header(
                                        "Authorization",
                                        "Bearer test-token"
                                )
                                .contentType("application/json")
                                .content(firstBookingJson)
                )
                .andExpect(status().isCreated());

        String secondBookingJson = """
            {
                "roomId": %d,
                "startDate": "2030-12-01",
                "endDate": "2030-12-05",
                "extraBed": false
            }
            """.formatted(testRoom.getId());

        mockMvc.perform(
                        post("/bookings")
                                .with(asCustomer(2L))
                                .header(
                                        "Authorization",
                                        "Bearer test-token"
                                )
                                .contentType("application/json")
                                .content(secondBookingJson)
                )
                .andExpect(status().isConflict());
    }
    private RequestPostProcessor asCustomer(Long customerId) {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        customerId,
                        null,
                        Collections.emptyList()
                );

        return authentication(authentication);
    }
}