package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

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
        return toDtosWithItems(requestRepository.findByRequester(userId));
    }

    @Override
    public List<ItemRequestDto> findAllExceptRequester(Long userId) {
        checkUserExists(userId);
        return toDtosWithItems(requestRepository.findAllExceptRequester(userId));
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        checkUserExists(userId);

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        "Request not found: " + requestId
                ));

        return toDtosWithItems(List.of(request)).getFirst();
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
    }

    private List<ItemRequestDto> toDtosWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<ItemRequestResponseDto>> itemsByRequest = itemRepository
                .findByRequestIds(requestIds)
                .stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(
                                item -> new ItemRequestResponseDto(
                                        item.getId(),
                                        item.getName(),
                                        item.getOwner().getId()
                                ),
                                Collectors.toList()
                        )
                ));

        return requests.stream()
                .map(request -> ItemRequestMapper.toDto(
                        request,
                        itemsByRequest.getOrDefault(request.getId(), List.of())
                ))
                .toList();
    }
}
