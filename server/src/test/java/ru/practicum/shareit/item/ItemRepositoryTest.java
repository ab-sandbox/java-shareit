package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;

    private User owner;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(
                new User(null, "Alexey", "alexey@example.com")
        );

        User requester = userRepository.save(
                new User(null, "Ivan", "ivan@example.com")
        );

        request = new ItemRequest();
        request.setDescription("Нужны инструменты");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        request = requestRepository.save(request);
    }

    @Test
    void shouldFindItemsByRequestIds() {
        Item drill = saveItem("Дрель", request);
        Item ladder = saveItem("Лестница", request);
        saveItem("Велосипед", null);

        List<Item> result = itemRepository.findByRequestIds(
                List.of(request.getId())
        );

        assertEquals(2, result.size());

        List<Long> ids = result.stream()
                .map(Item::getId)
                .toList();

        assertTrue(ids.contains(drill.getId()));
        assertTrue(ids.contains(ladder.getId()));

        assertTrue(result.stream()
                .allMatch(item ->
                        item.getOwner().getId().equals(owner.getId())
                ));
    }

    @Test
    void shouldFindItemsForMultipleRequests() {
        ItemRequest secondRequest = new ItemRequest();
        secondRequest.setDescription("Нужен транспорт");
        secondRequest.setRequester(request.getRequester());
        secondRequest.setCreated(LocalDateTime.now());

        secondRequest = requestRepository.save(secondRequest);

        saveItem("Дрель", request);
        saveItem("Велосипед", secondRequest);

        List<Item> result = itemRepository.findByRequestIds(
                List.of(request.getId(), secondRequest.getId())
        );

        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoItemsMatch() {
        saveItem("Дрель", null);

        List<Item> result = itemRepository.findByRequestIds(
                List.of(request.getId())
        );

        assertTrue(result.isEmpty());
    }

    private Item saveItem(String name, ItemRequest itemRequest) {
        return itemRepository.save(
                new Item(
                        null,
                        name,
                        "Описание: " + name,
                        true,
                        owner,
                        itemRequest
                )
        );
    }
}
