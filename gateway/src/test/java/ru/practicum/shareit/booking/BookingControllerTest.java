
package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
