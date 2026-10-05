package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        checkEmailUnique(userDto.getEmail(), null);

        User user = UserMapper.toUser(userDto);
        user.setId(null);

        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existing = getUser(id);

        if (userDto.getEmail() != null) {
            checkEmailUnique(userDto.getEmail(), id);
        }

        User updated = new User(
                existing.getId(),
                userDto.getName() != null
                        ? userDto.getName()
                        : existing.getName(),
                userDto.getEmail() != null
                        ? userDto.getEmail()
                        : existing.getEmail()
        );

        return UserMapper.toDto(userRepository.save(updated));
    }

    @Override
    public UserDto findById(Long id) {
        return UserMapper.toDto(getUser(id));
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        getUser(id);
        userRepository.deleteById(id);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с ID " + id + " не найден"
                ));
    }

    private void checkEmailUnique(String email, Long currentUserId) {
        userRepository.findByEmail(email)
                .filter(user -> !user.getId().equals(currentUserId))
                .ifPresent(user -> {
                    throw new ConflictException(
                            "Email " + email + " уже используется"
                    );
                });
    }
}
