package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {

    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private ItemService itemService;
    private BookingRepository bookingRepository;
    private CommentRepository commentRepository;

    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        bookingRepository = mock(BookingRepository.class);
        commentRepository = mock(CommentRepository.class);

        itemService = new ItemServiceImpl(
                itemRepository,
                userRepository,
                bookingRepository,
                commentRepository
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

    @Test
    void shouldAddCommentAfterCompletedBooking() {
        Long userId = 2L;
        Long itemId = 1L;

        User author = new User(
                userId, "Ivan", "ivan@example.com"
        );

        User owner = new User(
                1L, "Alexey", "alexey@example.com"
        );

        Item item = new Item(
                itemId,
                "Дрель",
                "Аккумуляторная дрель",
                true,
                owner,
                null
        );

        CommentCreateDto request = new CommentCreateDto(
                "Отличная дрель!"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(author));

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        when(bookingRepository.existsCompletedBooking(
                eq(itemId),
                eq(userId),
                eq(BookingStatus.APPROVED),
                any(LocalDateTime.class)
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment comment = invocation.getArgument(0);
                    comment.setId(10L);
                    return comment;
                });

        CommentDto result = itemService.addComment(
                userId, itemId, request
        );

        assertEquals(10L, result.getId());
        assertEquals("Отличная дрель!", result.getText());
        assertEquals("Ivan", result.getAuthorName());
        assertNotNull(result.getCreated());

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldNotAddCommentWithoutCompletedBooking() {
        Long userId = 2L;
        Long itemId = item.getId();

        User author = new User(
                userId, "Ivan", "ivan@example.com"
        );

        CommentCreateDto request = new CommentCreateDto(
                "Отличная дрель!"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(author));

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        when(bookingRepository.existsCompletedBooking(
                eq(itemId),
                eq(userId),
                eq(BookingStatus.APPROVED),
                any(LocalDateTime.class)
        )).thenReturn(false);

        assertThrows(
                BadRequestException.class,
                () -> itemService.addComment(userId, itemId, request)
        );

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void shouldNotAddCommentWithBlankText() {
        Long userId = 2L;
        Long itemId = item.getId();

        CommentCreateDto request = new CommentCreateDto("   ");

        assertThrows(
                BadRequestException.class,
                () -> itemService.addComment(userId, itemId, request)
        );

        verifyNoInteractions(
                userRepository,
                itemRepository,
                bookingRepository,
                commentRepository
        );
    }

    @Test
    void shouldFindItemWithComments() {
        Long itemId = item.getId();
        LocalDateTime created = LocalDateTime.of(
                2026, 10, 25, 10, 30
        );

        User author = new User(
                2L, "Ivan", "ivan@example.com"
        );

        Comment comment = new Comment();
        comment.setId(10L);
        comment.setText("Отличная дрель!");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(created);

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        when(commentRepository.findAllByItemIdOrderByCreatedAsc(itemId))
                .thenReturn(List.of(comment));

        ItemDto result = itemService.findById(itemId);

        assertEquals(itemId, result.getId());
        assertEquals("Дрель", result.getName());

        assertEquals(1, result.getComments().size());

        CommentDto commentDto = result.getComments().get(0);

        assertEquals(10L, commentDto.getId());
        assertEquals("Отличная дрель!", commentDto.getText());
        assertEquals("Ivan", commentDto.getAuthorName());
        assertEquals(created, commentDto.getCreated());

        verify(commentRepository)
                .findAllByItemIdOrderByCreatedAsc(itemId);
    }

    @Test
    void shouldFindOwnerItemsWithComments() {
        Item secondItem = new Item(
                2L, "Шуруповерт", "Аккумуляторный", true, owner, null
        );

        User author = new User(2L, "Ivan", "ivan@example.com");

        Comment firstComment = new Comment();
        firstComment.setId(10L);
        firstComment.setText("Хорошая дрель");
        firstComment.setItem(item);
        firstComment.setAuthor(author);
        firstComment.setCreated(LocalDateTime.now().minusDays(2));

        Comment secondComment = new Comment();
        secondComment.setId(11L);
        secondComment.setText("Отличный шуруповерт");
        secondComment.setItem(secondItem);
        secondComment.setAuthor(author);
        secondComment.setCreated(LocalDateTime.now().minusDays(1));

        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        when(itemRepository.findAllByOwnerId(owner.getId()))
                .thenReturn(List.of(item, secondItem));

        when(commentRepository.findAllByItemIdInOrderByCreatedAsc(
                List.of(1L, 2L)
        )).thenReturn(List.of(firstComment, secondComment));

        List<ItemDto> result = itemService.findAllByOwner(owner.getId());

        assertEquals(2, result.size());

        assertEquals(1, result.get(0).getComments().size());
        assertEquals("Хорошая дрель",
                result.get(0).getComments().get(0).getText());

        assertEquals(1, result.get(1).getComments().size());
        assertEquals("Отличный шуруповерт",
                result.get(1).getComments().get(0).getText());

        verify(commentRepository, times(1))
                .findAllByItemIdInOrderByCreatedAsc(List.of(1L, 2L));
    }

    private void mockExistingItem() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
    }
}
