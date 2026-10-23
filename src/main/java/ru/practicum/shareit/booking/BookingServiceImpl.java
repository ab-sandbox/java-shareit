package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(Long bookerId, BookingCreateDto dto) {
        validateDates(dto);

        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь не найден: " + bookerId
                ));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException(
                        "Вещь не найдена: " + dto.getItemId()
                ));

        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException(
                    "Нельзя бронировать собственную вещь"
            );
        }

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new IllegalArgumentException(
                    "Вещь недоступна для бронирования"
            );
        }

        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(
                        "Бронирование не найдено: " + bookingId
                ));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new NotFoundException(
                    "Пользователь не является владельцем вещи"
            );
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException(
                    "Решение по бронированию уже принято"
            );
        }

        booking.setStatus(
                approved ? BookingStatus.APPROVED : BookingStatus.REJECTED
        );

        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> findByBooker(
            Long bookerId,
            BookingState state
    ) {
        validateUser(bookerId);

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBooker(bookerId);
            case CURRENT -> bookingRepository.findCurrentByBooker(bookerId, now);
            case PAST -> bookingRepository.findPastByBooker(bookerId, now);
            case FUTURE -> bookingRepository.findFutureByBooker(bookerId, now);
            case WAITING -> bookingRepository.findByBookerAndStatus(
                    bookerId, BookingStatus.WAITING
            );
            case REJECTED -> bookingRepository.findByBookerAndStatus(
                    bookerId, BookingStatus.REJECTED
            );
        };

        return bookings.stream()
                .map(BookingMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> findByOwner(
            Long ownerId,
            BookingState state
    ) {
        validateUser(ownerId);

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByOwner(ownerId);
            case CURRENT -> bookingRepository.findCurrentByOwner(ownerId, now);
            case PAST -> bookingRepository.findPastByOwner(ownerId, now);
            case FUTURE -> bookingRepository.findFutureByOwner(ownerId, now);
            case WAITING -> bookingRepository.findByOwnerAndStatus(
                    ownerId, BookingStatus.WAITING
            );
            case REJECTED -> bookingRepository.findByOwnerAndStatus(
                    ownerId, BookingStatus.REJECTED
            );
        };

        return bookings.stream()
                .map(BookingMapper::toDto)
                .toList();
    }

    private void validateDates(BookingCreateDto dto) {
        LocalDateTime now = LocalDateTime.now();

        if (dto.getStart() == null || dto.getEnd() == null) {
            throw new IllegalArgumentException(
                    "Необходимо указать начало и окончание бронирования"
            );
        }

        if (!dto.getStart().isAfter(now)) {
            throw new IllegalArgumentException(
                    "Дата начала должна быть в будущем"
            );
        }

        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new IllegalArgumentException(
                    "Дата окончания должна быть позже даты начала"
            );
        }
    }

    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(
                    "Пользователь не найден: " + userId
            );
        }
    }
}
