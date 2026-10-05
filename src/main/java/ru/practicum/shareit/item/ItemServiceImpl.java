package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = getUser(userId);

        Item item = ItemMapper.toItem(itemDto);
        item.setId(null);
        item.setOwner(owner);

        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item existing = getItem(itemId);

        if (!existing.getOwner().getId().equals(userId)) {
            throw new NotFoundException(
                    "Вещь с ID " + itemId + " не найдена у пользователя " + userId
            );
        }

        Item updated = new Item(
                existing.getId(),
                itemDto.getName() != null
                        ? itemDto.getName()
                        : existing.getName(),
                itemDto.getDescription() != null
                        ? itemDto.getDescription()
                        : existing.getDescription(),
                itemDto.getAvailable() != null
                        ? itemDto.getAvailable()
                        : existing.getAvailable(),
                existing.getOwner()
        );

        return ItemMapper.toDto(itemRepository.save(updated));
    }

    @Override
    public ItemDto findById(Long itemId) {
        return ItemMapper.toDto(getItem(itemId));
    }

    @Override
    public List<ItemDto> findAllByOwner(Long userId) {
        getUser(userId);

        return itemRepository.findAllByOwnerId(userId).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemRepository.search(text).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с ID " + userId + " не найден"
                ));
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        "Вещь с ID " + itemId + " не найдена"
                ));
    }
}
