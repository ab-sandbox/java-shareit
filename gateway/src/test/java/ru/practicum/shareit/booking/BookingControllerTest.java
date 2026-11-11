
package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @ParameterizedTest
    @MethodSource("invalidBookingRequests")
    void shouldRejectInvalidBookingRequest(BookingCreateDto request) throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }


    @Test
    void shouldCreateBooking() throws Exception {
        when(bookingClient.create(eq(1L), any(BookingCreateDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 10)));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content("""
                                {
                                  "itemId": 2,
                                  "start": "2027-01-10T10:00:00",
                                  "end": "2027-01-11T10:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(bookingClient).create(
                eq(1L), any(BookingCreateDto.class)
        );
    }

    @Test
    void shouldApproveBooking() throws Exception {
        when(bookingClient.approve(1L, 10L, true))
                .thenReturn(ResponseEntity.ok(Map.of(
                        "id", 10,
                        "status", "APPROVED"
                )));

        mockMvc.perform(patch("/bookings/10")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingClient).approve(1L, 10L, true);
    }

    @Test
    void shouldRejectBooking() throws Exception {
        when(bookingClient.approve(1L, 10L, false))
                .thenReturn(ResponseEntity.ok(Map.of(
                        "id", 10,
                        "status", "REJECTED"
                )));

        mockMvc.perform(patch("/bookings/10")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(bookingClient).approve(1L, 10L, false);
    }

    @Test
    void shouldGetBookingById() throws Exception {
        when(bookingClient.getById(1L, 10L))
                .thenReturn(ResponseEntity.ok(Map.of("id", 10)));

        mockMvc.perform(get("/bookings/10")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(bookingClient).getById(1L, 10L);
    }

    @Test
    void shouldFindBookingsByBookerWithDefaultState() throws Exception {
        when(bookingClient.findByBooker(1L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(
                        List.of(Map.of("id", 10))
                ));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));

        verify(bookingClient).findByBooker(1L, BookingState.ALL);
    }

    @Test
    void shouldFindBookingsByBookerWithLowercaseState() throws Exception {
        when(bookingClient.findByBooker(1L, BookingState.CURRENT))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(bookingClient).findByBooker(
                1L, BookingState.CURRENT
        );
    }

    @Test
    void shouldFindBookingsByOwner() throws Exception {
        when(bookingClient.findByOwner(1L, BookingState.FUTURE))
                .thenReturn(ResponseEntity.ok(
                        List.of(Map.of("id", 10))
                ));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));

        verify(bookingClient).findByOwner(
                1L, BookingState.FUTURE
        );
    }

    @Test
    void shouldFindBookingsByOwnerWithDefaultState() throws Exception {
        when(bookingClient.findByOwner(1L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(bookingClient).findByOwner(1L, BookingState.ALL);
    }

    private static Stream<BookingCreateDto> invalidBookingRequests() {
        LocalDateTime future = LocalDateTime.now().plusDays(2);
        LocalDateTime past = LocalDateTime.now().minusDays(1);

        return Stream.of(
                new BookingCreateDto(null, future, future.plusDays(1)),
                new BookingCreateDto(1L, null, future),
                new BookingCreateDto(1L, future, null),
                new BookingCreateDto(1L, past, future),
                new BookingCreateDto(1L, future, past)
        );
    }
}
