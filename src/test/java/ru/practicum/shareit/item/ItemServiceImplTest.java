package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {

    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private ItemService itemService;

    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);

        itemService = new ItemServiceImpl(
                itemRepository,
                userRepository
        );

        owner = new User(
                1L, "Alexey", "alexey@example.com"
        );

        item = new Item(
                1L,
                "Дрель",
                "Аккумуляторная дрель",
                true,
                owner,
                null
        );
    }

    @Test
    void shouldCreateItem() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    Item saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        ItemDto created = itemService.create(
                owner.getId(),
                new ItemDto(
                        null,
                        "Дрель",
                        "Аккумуляторная дрель",
                        true
                )
        );

        assertEquals(1L, created.getId());
        assertEquals("Дрель", created.getName());
        assertEquals("Аккумуляторная дрель",
                created.getDescription());
        assertTrue(created.getAvailable());

        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void shouldUpdateOnlySpecifiedFields() {
        mockExistingItem();

        ItemDto updated = itemService.update(
                owner.getId(),
                item.getId(),
                new ItemDto(null, null, null, false)
        );

        assertEquals("Дрель", updated.getName());
        assertEquals("Аккумуляторная дрель",
                updated.getDescription());
        assertFalse(updated.getAvailable());

        assertFalse(item.getAvailable());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void shouldThrowWhenAnotherUserUpdatesItem() {
        User anotherUser = new User(
                2L, "Ivan", "ivan@example.com"
        );

        when(userRepository.findById(anotherUser.getId()))
                .thenReturn(Optional.of(anotherUser));

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        assertThrows(
                NotFoundException.class,
                () -> itemService.update(
                        anotherUser.getId(),
                        item.getId(),
                        new ItemDto(
                                null, "Чужая дрель", null, null
                        )
                )
        );

        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void shouldFindItemsByOwner() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        when(itemRepository.findAllByOwnerId(owner.getId()))
                .thenReturn(List.of(item));

        List<ItemDto> items =
                itemService.findAllByOwner(owner.getId());

        assertEquals(1, items.size());
        assertEquals("Дрель", items.get(0).getName());
    }

    @Test
    void shouldSearchAvailableItemsIgnoringCase() {
        when(itemRepository.search("ДРЕЛЬ"))
                .thenReturn(List.of(item));

        List<ItemDto> items = itemService.search("ДРЕЛЬ");

        assertEquals(1, items.size());
        assertEquals("Дрель", items.get(0).getName());

        verify(itemRepository).search("ДРЕЛЬ");
    }

    @Test
    void shouldNotFindUnavailableItems() {
        when(itemRepository.search("дрель"))
                .thenReturn(List.of());

        List<ItemDto> items = itemService.search("дрель");

        assertTrue(items.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForBlankSearch() {
        List<ItemDto> items = itemService.search("   ");

        assertTrue(items.isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void shouldSearchItemByDescription() {
        when(itemRepository.search("аккумуляторная"))
                .thenReturn(List.of(item));

        List<ItemDto> items =
                itemService.search("аккумуляторная");

        assertEquals(1, items.size());
        assertEquals("Дрель", items.get(0).getName());

        verify(itemRepository).search("аккумуляторная");
    }

    @Test
    void shouldThrowWhenUpdatingItemWithUnknownUser() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.update(
                        999L,
                        item.getId(),
                        new ItemDto(
                                null, "Новая дрель", null, null
                        )
                )
        );

        assertTrue(exception.getMessage()
                .contains("Пользователь"));

        verify(itemRepository, never()).save(any(Item.class));
    }

    private void mockExistingItem() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
    }
}
