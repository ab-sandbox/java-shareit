package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceImplTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = new InMemoryUserRepository();
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void shouldCreateUser() {
        UserDto userDto = new UserDto(
                null,
                "Alexey",
                "alexey@example.com"
        );

        UserDto created = userService.create(userDto);

        assertEquals(1L, created.getId());
        assertEquals("Alexey", created.getName());
        assertEquals("alexey@example.com", created.getEmail());
    }

    @Test
    void shouldFindUserById() {
        UserDto created = userService.create(new UserDto(
                null,
                "Alexey",
                "alexey@example.com"
        ));

        UserDto found = userService.findById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getName(), found.getName());
        assertEquals(created.getEmail(), found.getEmail());
    }

    @Test
    void shouldUpdateOnlySpecifiedFields() {
        UserDto created = userService.create(new UserDto(
                null,
                "Alexey",
                "alexey@example.com"
        ));

        UserDto updated = userService.update(
                created.getId(),
                new UserDto(null, "Alex", null)
        );

        assertEquals("Alex", updated.getName());
        assertEquals("alexey@example.com", updated.getEmail());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> userService.findById(999L)
        );
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        userService.create(new UserDto(
                null,
                "Alexey",
                "alexey@example.com"
        ));

        assertThrows(
                ConflictException.class,
                () -> userService.create(new UserDto(
                        null,
                        "Another user",
                        "alexey@example.com"
                ))
        );
    }

    @Test
    void shouldFindAllUsers() {
        userService.create(new UserDto(
                null,
                "Alexey",
                "alexey@example.com"
        ));

        userService.create(new UserDto(
                null,
                "Ivan",
                "ivan@example.com"
        ));

        assertEquals(2, userService.findAll().size());
    }

    @Test
    void shouldDeleteUser() {
        UserDto created = userService.create(new UserDto(
                null,
                "Alexey",
                "alexey@example.com"
        ));

        userService.delete(created.getId());

        assertThrows(
                NotFoundException.class,
                () -> userService.findById(created.getId())
        );
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() {
        userService.create(new UserDto(
                null,
                "Иван",
                "Ivan@mail.ru"
        ));

        UserDto duplicate = new UserDto(
                null,
                "Другой Иван",
                "ivan@mail.ru"
        );

        assertThrows(
                ConflictException.class,
                () -> userService.create(duplicate)
        );
    }
}
