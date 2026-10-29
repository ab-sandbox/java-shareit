package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = getUser(userId);

        Item item = ItemMapper.toItem(itemDto);
        item.setId(null);
        item.setOwner(owner);

        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        getUser(userId);

        Item item = getItem(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException(
                    "Вещь с ID " + itemId + " не найдена у пользователя " + userId
            );
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto findById(Long itemId, Long userId) {
        Item item = getItem(itemId);
        ItemDto dto = ItemMapper.toDto(item);

        List<CommentDto> comments = commentRepository
                .findAllByItemIdOrderByCreatedAsc(itemId)
                .stream()
                .map(CommentMapper::toDto)
                .toList();

        dto.setComments(comments);

        if (item.getOwner().getId().equals(userId)) {
            fillBookings(dto, itemId);
        }

        return dto;
    }

    @Override
    public List<ItemDto> findAllByOwner(Long userId) {
        getUser(userId);

        List<Item> items = itemRepository.findAllByOwnerId(userId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        Map<Long, List<CommentDto>> commentsByItem =
                commentRepository.findAllByItemIdInOrderByCreatedAsc(itemIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                comment -> comment.getItem().getId(),
                                Collectors.mapping(
                                        CommentMapper::toDto,
                                        Collectors.toList()
                                )
                        ));

        return items.stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toDto(item);
                    dto.setComments(
                            commentsByItem.getOrDefault(item.getId(), List.of())
                    );
                    fillBookings(dto, item.getId());
                    return dto;
                })
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(
            Long userId,
            Long itemId,
            CommentCreateDto dto
    ) {
        if (dto == null || dto.getText() == null
            || dto.getText().isBlank()) {
            throw new BadRequestException(
                    "Текст комментария не может быть пустым"
            );
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь не найден: " + userId
                ));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        "Вещь не найдена: " + itemId
                ));

        boolean completedBooking =
                bookingRepository.existsCompletedBooking(
                        itemId,
                        userId,
                        BookingStatus.APPROVED,
                        LocalDateTime.now()
                );

        if (!completedBooking) {
            throw new BadRequestException(
                    "Оставить комментарий можно только после аренды вещи"
            );
        }

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toDto(commentRepository.save(comment));
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

    private void fillBookings(ItemDto dto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();

        bookingRepository.findLastBooking(itemId, BookingStatus.APPROVED, now)
                .ifPresent(booking -> dto.setLastBooking(toShortDto(booking)));

        bookingRepository.findNextBooking(itemId, BookingStatus.APPROVED, now)
                .ifPresent(booking -> dto.setNextBooking(toShortDto(booking)));
    }

    private BookingShortDto toShortDto(Booking booking) {
        return new BookingShortDto(
                booking.getId(),
                booking.getBooker().getId()
        );
    }
}
