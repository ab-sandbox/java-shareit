package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User not found: " + userId
                ));

        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        return ItemRequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ItemRequestDto> findByRequester(Long userId) {
        checkUserExists(userId);

        return requestRepository.findByRequester(userId).stream()
                .map(ItemRequestMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemRequestDto> findAllExceptRequester(Long userId) {
        checkUserExists(userId);

        return requestRepository.findAllExceptRequester(userId).stream()
                .map(ItemRequestMapper::toDto)
                .toList();
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        checkUserExists(userId);

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        "Request not found: " + requestId
                ));

        return ItemRequestMapper.toDto(request);
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
    }
}
