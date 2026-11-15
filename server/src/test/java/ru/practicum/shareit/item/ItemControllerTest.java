package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void shouldCreateItem() throws Exception {
        ItemDto dto = item(1L);

        when(itemService.create(eq(1L), any(ItemDto.class)))
                .thenReturn(dto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));

        verify(itemService).create(eq(1L), any(ItemDto.class));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemDto dto = item(1L);
        dto.setName("Новая дрель");

        when(itemService.update(eq(1L), eq(1L), any(ItemDto.class)))
                .thenReturn(dto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новая дрель"));

        verify(itemService).update(
                eq(1L), eq(1L), any(ItemDto.class)
        );
    }

    @Test
    void shouldFindItemById() throws Exception {
        when(itemService.findById(1L, 2L))
                .thenReturn(item(1L));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(itemService).findById(1L, 2L);
    }

    @Test
    void shouldFindItemByIdWithoutUserHeader() throws Exception {
        when(itemService.findById(1L, null))
                .thenReturn(item(1L));

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(itemService).findById(1L, null);
    }

    @Test
    void shouldFindAllItemsByOwner() throws Exception {
        when(itemService.findAllByOwner(1L))
                .thenReturn(List.of(item(1L)));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(itemService).findAllByOwner(1L);
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemService.search("дрель"))
                .thenReturn(List.of(item(1L)));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));

        verify(itemService).search("дрель");
    }

    @Test
    void shouldAddComment() throws Exception {
        CommentCreateDto request = new CommentCreateDto("Отличная дрель");

        CommentDto response = new CommentDto(1L, "Отличная дрель", null, null);

        when(itemService.addComment(
                eq(2L), eq(1L), any(CommentCreateDto.class)
        )).thenReturn(response);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Отличная дрель"));

        verify(itemService).addComment(
                eq(2L), eq(1L), any(CommentCreateDto.class)
        );
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
