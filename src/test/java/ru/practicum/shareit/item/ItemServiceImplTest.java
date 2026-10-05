package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.InMemoryUserRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemServiceImplTest {

    private ItemService itemService;
    private UserRepository userRepository;
    private Long ownerId;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        ItemRepository itemRepository = new InMemoryItemRepository();

        itemService = new ItemServiceImpl(
                itemRepository,
                userRepository
        );

        User owner = userRepository.save(new User(
                null,
                "Alexey",
                "alexey@example.com"
        ));

        ownerId = owner.getId();
    }

    @Test
    void shouldCreateItem() {
        ItemDto created = itemService.create(
                ownerId,
                new ItemDto(
                        null,
                        "Дрель",
                        "Аккумуляторная дрель",
                        true
                )
        );

        assertEquals(1L, created.getId());
        assertEquals("Дрель", created.getName());
        assertEquals("Аккумуляторная дрель", created.getDescription());
        assertTrue(created.getAvailable());
    }

    @Test
    void shouldUpdateOnlySpecifiedFields() {
        ItemDto created = createItem();

        ItemDto updated = itemService.update(
                ownerId,
                created.getId(),
                new ItemDto(null, null, null, false)
        );

        assertEquals("Дрель", updated.getName());
        assertEquals("Аккумуляторная дрель", updated.getDescription());
        assertFalse(updated.getAvailable());
    }

    @Test
    void shouldThrowWhenAnotherUserUpdatesItem() {
        User anotherUser = userRepository.save(new User(
                null,
                "Ivan",
                "ivan@example.com"
        ));

        ItemDto created = createItem();

        assertThrows(
                NotFoundException.class,
                () -> itemService.update(
                        anotherUser.getId(),
                        created.getId(),
                        new ItemDto(null, "Чужая дрель", null, null)
                )
        );
    }

    @Test
    void shouldFindItemsByOwner() {
        createItem();

        List<ItemDto> items = itemService.findAllByOwner(ownerId);

        assertEquals(1, items.size());
        assertEquals("Дрель", items.get(0).getName());
    }

    @Test
    void shouldSearchAvailableItemsIgnoringCase() {
        createItem();

        List<ItemDto> items = itemService.search("ДРЕЛЬ");

        assertEquals(1, items.size());
        assertEquals("Дрель", items.get(0).getName());
    }

    @Test
    void shouldNotFindUnavailableItems() {
        ItemDto created = createItem();

        itemService.update(
                ownerId,
                created.getId(),
                new ItemDto(null, null, null, false)
        );

        assertTrue(itemService.search("дрель").isEmpty());
    }

    @Test
    void shouldReturnEmptyListForBlankSearch() {
        assertTrue(itemService.search("   ").isEmpty());
    }

    @Test
    void shouldSearchItemByDescription() {
        createItem();

        List<ItemDto> items = itemService.search("аккумуляторная");

        assertEquals(1, items.size());
        assertEquals("Дрель", items.get(0).getName());
    }

    private ItemDto createItem() {
        return itemService.create(
                ownerId,
                new ItemDto(
                        null,
                        "Дрель",
                        "Аккумуляторная дрель",
                        true
                )
        );
    }
}
