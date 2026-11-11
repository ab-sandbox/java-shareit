package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void shouldCreateItem() throws Exception {
        ItemDto dto = item(1L);

        when(itemClient.create(eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));

        verify(itemClient).create(eq(1L), any(ItemDto.class));
    }

    @Test
    void shouldCreateItemWithRequestId() throws Exception {
        ItemDto dto = item(1L);
        dto.setRequestId(10L);

        when(itemClient.create(eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(10));

        verify(itemClient).create(
                eq(1L),
                argThat(item -> Long.valueOf(10L).equals(item.getRequestId()))
        );
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName("Новая дрель");

        when(itemClient.update(eq(1L), eq(2L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(patch("/items/2")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новая дрель"));

        verify(itemClient).update(
                eq(1L), eq(2L), any(ItemDto.class)
        );
    }

    @Test
    void shouldFindItemById() throws Exception {
        when(itemClient.findById(2L, 1L))
                .thenReturn(ResponseEntity.ok(item(2L)));

        mockMvc.perform(get("/items/2")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        verify(itemClient).findById(2L, 1L);
    }

    @Test
    void shouldFindItemWithoutUserHeader() throws Exception {
        when(itemClient.findById(2L, null))
                .thenReturn(ResponseEntity.ok(item(2L)));

        mockMvc.perform(get("/items/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        verify(itemClient).findById(2L, null);
    }

    @Test
    void shouldFindAllItemsByOwner() throws Exception {
        when(itemClient.findAllByOwner(1L))
                .thenReturn(ResponseEntity.ok(List.of(item(2L))));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));

        verify(itemClient).findAllByOwner(1L);
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemClient.search("дрель"))
                .thenReturn(ResponseEntity.ok(List.of(item(2L))));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));

        verify(itemClient).search("дрель");
    }

    @Test
    void shouldAddComment() throws Exception {
        CommentCreateDto dto = new CommentCreateDto("Отличная дрель");

        when(itemClient.addComment(
                eq(1L), eq(2L), any(CommentCreateDto.class)
        )).thenReturn(ResponseEntity.ok(
                java.util.Map.of("id", 3, "text", "Отличная дрель")
        ));

        mockMvc.perform(post("/items/2/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Отличная дрель"));

        verify(itemClient).addComment(
                eq(1L), eq(2L), any(CommentCreateDto.class)
        );
    }

    @Test
    void shouldRejectItemWithoutName() throws Exception {
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Аккумуляторная дрель",
                                  "available": true
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void shouldRejectItemWithoutAvailable() throws Exception {
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Дрель",
                                  "description": "Аккумуляторная дрель"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    private ItemDto item(Long id) {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setName("Дрель");
        dto.setDescription("Аккумуляторная дрель");
        dto.setAvailable(true);
        return dto;
    }
}
