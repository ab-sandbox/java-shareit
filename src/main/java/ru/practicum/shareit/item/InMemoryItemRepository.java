package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private long nextId = 1L;

    @Override
    public synchronized Item save(Item item) {
        if (item.getId() == null) {
            item.setId(nextId++);
        }

        items.put(item.getId(), item);
        return item;
    }

    @Override
    public synchronized Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public synchronized List<Item> findAllByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .toList();
    }

    @Override
    public synchronized List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        String query = text.toLowerCase(Locale.ROOT);

        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> containsIgnoreCase(item.getName(), query)
                                || containsIgnoreCase(item.getDescription(), query))
                .toList();
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null
               && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
