package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
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

    public ItemRequestDto toDto(ItemRequest request, List<ItemDto> items) {
        ItemRequestDto dto = toDto(request);
        dto.setItems(items);
        return dto;
    }
}
