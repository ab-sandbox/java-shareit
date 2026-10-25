package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void shouldCreateBooking() throws Exception {
        Long userId = 1L;
        Long bookingId = 10L;

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);

        BookingCreateDto request = new BookingCreateDto(
                5L, start, end
        );

        BookingDto response = new BookingDto(
                bookingId,
                start,
                end,
                null,
                null,
                BookingStatus.WAITING
        );

        when(bookingService.create(
                eq(userId),
                any(BookingCreateDto.class)
        )).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("WAITING"));

        ArgumentCaptor<BookingCreateDto> captor =
                ArgumentCaptor.forClass(BookingCreateDto.class);

        verify(bookingService).create(eq(userId), captor.capture());

        BookingCreateDto actual = captor.getValue();

        assertEquals(request.getItemId(), actual.getItemId());
        assertEquals(request.getStart(), actual.getStart());
        assertEquals(request.getEnd(), actual.getEnd());
    }

    @Test
    void shouldGetBookingById() throws Exception {
        Long userId = 1L;
        Long bookingId = 10L;

        BookingDto response = new BookingDto(
                bookingId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null,
                null,
                BookingStatus.WAITING
        );

        when(bookingService.getById(userId, bookingId))
                .thenReturn(response);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).getById(userId, bookingId);
    }

    @Test
    void shouldApproveBooking() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 10L;

        BookingDto response = new BookingDto(
                bookingId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null,
                null,
                BookingStatus.APPROVED
        );

        when(bookingService.approve(ownerId, bookingId, true))
                .thenReturn(response);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).approve(ownerId, bookingId, true);
    }

    @Test
    void shouldFindBookingsByBooker() throws Exception {
        Long userId = 1L;

        BookingDto booking = new BookingDto(
                10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null,
                null,
                BookingStatus.WAITING
        );

        when(bookingService.findByBooker(userId, BookingState.FUTURE))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("WAITING"));

        verify(bookingService).findByBooker(userId, BookingState.FUTURE);
    }

    @Test
    void shouldUseAllStateByDefault() throws Exception {
        Long userId = 1L;

        when(bookingService.findByBooker(userId, BookingState.ALL))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookingService).findByBooker(userId, BookingState.ALL);
    }

    @Test
    void shouldFindBookingsByOwner() throws Exception {
        Long ownerId = 1L;

        BookingDto booking = new BookingDto(
                10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null,
                null,
                BookingStatus.APPROVED
        );

        when(bookingService.findByOwner(ownerId, BookingState.CURRENT))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));

        verify(bookingService).findByOwner(ownerId, BookingState.CURRENT);
    }

    @Test
    void shouldReturnBadRequestForUnknownState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Неизвестное состояние бронирования: UNKNOWN"));

        verifyNoInteractions(bookingService);
    }

    @Test
    void shouldReturnNotFoundForMissingBooking() throws Exception {
        Long userId = 1L;
        Long bookingId = 999L;

        when(bookingService.getById(userId, bookingId))
                .thenThrow(new NotFoundException(
                        "Бронирование не найдено: " + bookingId
                ));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Бронирование не найдено: 999"));

        verify(bookingService).getById(userId, bookingId);
    }
}
