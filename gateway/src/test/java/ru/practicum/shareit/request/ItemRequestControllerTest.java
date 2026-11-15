package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

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
    private ItemRequestClient requestClient;

    @Test
    void shouldCreateRequest() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Нужна лестница");

        when(requestClient.create(eq(1L), any(ItemRequestCreateDto.class)))
                .thenReturn(ResponseEntity.status(201).build());

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(requestClient).create(eq(1L), any(ItemRequestCreateDto.class));
    }

    @Test
    void shouldRejectBlankDescription() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("   ");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @Test
    void shouldFindRequestsByRequester() throws Exception {
        when(requestClient.findByRequester(1L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(requestClient).findByRequester(1L);
    }

    @Test
    void shouldFindRequestsFromOtherUsers() throws Exception {
        when(requestClient.findAllExceptRequester(1L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(requestClient).findAllExceptRequester(1L);
    }

    @Test
    void shouldGetRequestById() throws Exception {
        when(requestClient.getById(1L, 10L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/{requestId}", 10L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(requestClient).getById(1L, 10L);
    }

    @Test
    void shouldRejectMissingUserHeader() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }
}
