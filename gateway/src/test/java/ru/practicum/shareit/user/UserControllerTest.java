package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserClient userClient;

    @Test
    void shouldCreateUser() throws Exception {
        UserDto dto = user(1L);

        when(userClient.createUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email")
                        .value("alexey@example.com"));

        verify(userClient).createUser(any(UserDto.class));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UserDto dto = user(1L);

        when(userClient.updateUser(eq(1L), any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alexey"));

        verify(userClient).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    void shouldFindUserById() throws Exception {
        when(userClient.getUser(1L))
                .thenReturn(ResponseEntity.ok(user(1L)));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userClient).getUser(1L);
    }

    @Test
    void shouldFindAllUsers() throws Exception {
        when(userClient.getUsers())
                .thenReturn(ResponseEntity.ok(List.of(user(1L))));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(userClient).getUsers();
    }

    @Test
    void shouldDeleteUser() throws Exception {
        when(userClient.deleteUser(1L))
                .thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userClient).deleteUser(1L);
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content("{\n" +
                                 "  \"name\": \"Alexey\",\n" +
                                 "  \"email\": \"invalid-email\"\n" +
                                 "}\n"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    private UserDto user(Long id) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName("Alexey");
        dto.setEmail("alexey@example.com");
        return dto;
    }
}
