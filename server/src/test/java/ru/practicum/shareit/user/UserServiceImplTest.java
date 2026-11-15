package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void shouldCreateUser() {
        UserDto userDto = new UserDto(
                null, "Alexey", "alexey@example.com"
        );

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        UserDto created = userService.create(userDto);

        assertEquals(1L, created.getId());
        assertEquals("Alexey", created.getName());
        assertEquals("alexey@example.com", created.getEmail());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldFindUserById() {
        User user = new User(
                1L, "Alexey", "alexey@example.com"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserDto found = userService.findById(1L);

        assertEquals(user.getId(), found.getId());
        assertEquals(user.getName(), found.getName());
        assertEquals(user.getEmail(), found.getEmail());
    }

    @Test
    void shouldUpdateOnlySpecifiedFields() {
        User user = new User(
                1L, "Alexey", "alexey@example.com"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserDto updated = userService.update(
                1L,
                new UserDto(null, "Alex", null)
        );

        assertEquals("Alex", updated.getName());
        assertEquals("alexey@example.com", updated.getEmail());

        assertEquals("Alex", user.getName());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> userService.findById(999L)
        );
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        User existing = new User(
                1L, "Alexey", "alexey@example.com"
        );

        when(userRepository.findByEmailIgnoreCase(
                "alexey@example.com"
        )).thenReturn(Optional.of(existing));

        assertThrows(
                ConflictException.class,
                () -> userService.create(new UserDto(
                        null, "Another user", "alexey@example.com"
                ))
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldFindAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(
                new User(1L, "Alexey", "alexey@example.com"),
                new User(2L, "Ivan", "ivan@example.com")
        ));

        List<UserDto> users = userService.findAll();

        assertEquals(2, users.size());
        assertEquals("Alexey", users.get(0).getName());
        assertEquals("Ivan", users.get(1).getName());
    }

    @Test
    void shouldDeleteUser() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User(
                        1L, "Alexey", "alexey@example.com"
                )));

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() {
        User existing = new User(
                1L, "Иван", "Ivan@mail.ru"
        );

        when(userRepository.findByEmailIgnoreCase(
                "ivan@mail.ru"
        )).thenReturn(Optional.of(existing));

        assertThrows(
                ConflictException.class,
                () -> userService.create(new UserDto(
                        null, "Другой Иван", "ivan@mail.ru"
                ))
        );

        verify(userRepository, never()).save(any(User.class));
    }
}
