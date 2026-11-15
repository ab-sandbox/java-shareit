package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void shouldCreateRequest() throws Exception {
        ItemRequestCreateDto request = new ItemRequestCreateDto();
        request.setDescription("Нужна лестница");

        ItemRequestDto response = new ItemRequestDto(
                10L, "Нужна лестница", LocalDateTime.now()
        );

        when(requestService.create(eq(1L), any(ItemRequestCreateDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description").value("Нужна лестница"));

        verify(requestService).create(eq(1L), any(ItemRequestCreateDto.class));
    }

    @Test
    void shouldFindRequestsByRequester() throws Exception {
        ItemRequestDto response = new ItemRequestDto(
                10L, "Нужна лестница", LocalDateTime.now()
        );

        when(requestService.findByRequester(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10));

        verify(requestService).findByRequester(1L);
    }

    @Test
    void shouldFindRequestsFromOtherUsers() throws Exception {
        ItemRequestDto response = new ItemRequestDto(
                10L, "Нужна лестница", LocalDateTime.now()
        );

        when(requestService.findAllExceptRequester(2L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10));

        verify(requestService).findAllExceptRequester(2L);
    }

    @Test
    void shouldGetRequestById() throws Exception {
        ItemRequestDto response = new ItemRequestDto(
                10L, "Нужна лестница", LocalDateTime.now()
        );

        when(requestService.getById(2L, 10L))
                .thenReturn(response);

        mockMvc.perform(get("/requests/{requestId}", 10L)
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description").value("Нужна лестница"));

        verify(requestService).getById(2L, 10L);
    }

    @Test
    void shouldReturnNotFoundForMissingRequest() throws Exception {
        when(requestService.getById(1L, 999L))
                .thenThrow(new NotFoundException("Request not found: 999"));

        mockMvc.perform(get("/requests/{requestId}", 999L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Request not found: 999"));
    }

    @Test
    void shouldRejectMissingUserHeader() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestService);
    }
}
