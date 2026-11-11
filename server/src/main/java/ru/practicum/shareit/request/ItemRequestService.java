package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, ItemRequestCreateDto dto);

    List<ItemRequestDto> findByRequester(Long userId);

    List<ItemRequestDto> findAllExceptRequester(Long userId);

    ItemRequestDto getById(Long userId, Long requestId);
}
