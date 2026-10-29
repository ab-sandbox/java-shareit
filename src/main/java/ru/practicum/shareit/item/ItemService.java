package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemDto findById(Long itemId, Long userId);

    List<ItemDto> findAllByOwner(Long userId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto);
}
