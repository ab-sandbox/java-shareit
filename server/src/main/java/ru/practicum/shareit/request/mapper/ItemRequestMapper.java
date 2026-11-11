package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@UtilityClass
public class ItemRequestMapper {

    public ItemRequestDto toDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated()
        );
    }
}
