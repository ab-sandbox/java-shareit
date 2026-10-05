package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.OnCreate;
import ru.practicum.shareit.user.dto.OnUpdate;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto create(
            @RequestBody @Validated(OnCreate.class) UserDto userDto) {
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(
            @PathVariable Long userId,
            @RequestBody @Validated(OnUpdate.class) UserDto userDto) {
        return userService.update(userId, userDto);
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Long userId) {
        return userService.findById(userId);
    }

    @GetMapping
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}
