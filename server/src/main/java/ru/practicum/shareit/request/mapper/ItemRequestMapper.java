package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@UtilityClass
public class ItemRequestMapper {

    public ItemRequestDto toDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated()
        );
    }

    public ItemRequestDto toDto(ItemRequest request, List<ItemRequestResponseDto> items) {
        ItemRequestDto dto = toDto(request);
        dto.setItems(items);
        return dto;
    }
}
