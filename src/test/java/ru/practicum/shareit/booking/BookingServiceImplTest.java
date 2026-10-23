package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    private BookingRepository bookingRepository;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private BookingService bookingService;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);

        bookingService = new BookingServiceImpl(
                bookingRepository,
                itemRepository,
                userRepository
        );

        owner = new User(
                1L, "Alexey", "alexey@example.com"
        );

        booker = new User(
                2L, "Ivan", "ivan@example.com"
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
    void shouldCreateBookingWithWaitingStatus() {
        mockExistingUserAndItem();

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        BookingDto created = bookingService.create(
                booker.getId(),
                createBookingDto()
        );

        assertEquals(1L, created.getId());
        assertEquals(BookingStatus.WAITING, created.getStatus());
        assertEquals(item.getId(), created.getItem().getId());
        assertEquals(booker.getId(), created.getBooker().getId());

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldThrowWhenBookerDoesNotExist() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> bookingService.create(999L, createBookingDto())
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenItemDoesNotExist() {
        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));

        when(itemRepository.findById(999L))
                .thenReturn(Optional.empty());

        BookingCreateDto dto = createBookingDto();
        dto.setItemId(999L);

        assertThrows(
                NotFoundException.class,
                () -> bookingService.create(booker.getId(), dto)
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenBookingOwnItem() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        assertThrows(
                NotFoundException.class,
                () -> bookingService.create(
                        owner.getId(),
                        createBookingDto()
                )
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenItemIsUnavailable() {
        mockExistingUserAndItem();
        item.setAvailable(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.create(
                        booker.getId(),
                        createBookingDto()
                )
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenStartIsInPast() {
        BookingCreateDto dto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.create(booker.getId(), dto)
        );

        verifyNoInteractions(
                bookingRepository,
                itemRepository,
                userRepository
        );
    }

    @Test
    void shouldThrowWhenEndIsBeforeStart() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);

        BookingCreateDto dto = new BookingCreateDto(
                item.getId(),
                start,
                start.minusDays(1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.create(booker.getId(), dto)
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenDatesAreMissing() {
        BookingCreateDto dto = new BookingCreateDto(
                item.getId(),
                null,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.create(booker.getId(), dto)
        );

        verify(bookingRepository, never()).save(any());
    }


    @Test
    void shouldApproveBooking() {
        Booking booking = createWaitingBooking();

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        BookingDto updated = bookingService.approve(
                owner.getId(),
                booking.getId(),
                true
        );

        assertEquals(BookingStatus.APPROVED, updated.getStatus());
        assertEquals(BookingStatus.APPROVED, booking.getStatus());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void shouldRejectBooking() {
        Booking booking = createWaitingBooking();

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        BookingDto updated = bookingService.approve(
                owner.getId(),
                booking.getId(),
                false
        );

        assertEquals(BookingStatus.REJECTED, updated.getStatus());
        assertEquals(BookingStatus.REJECTED, booking.getStatus());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void shouldThrowWhenAnotherUserApprovesBooking() {
        Booking booking = createWaitingBooking();

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        assertThrows(
                NotFoundException.class,
                () -> bookingService.approve(
                        booker.getId(),
                        booking.getId(),
                        true
                )
        );

        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }

    @Test
    void shouldThrowWhenBookingAlreadyApproved() {
        Booking booking = createWaitingBooking();
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.approve(
                        owner.getId(),
                        booking.getId(),
                        true
                )
        );

        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    private void mockExistingUserAndItem() {
        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
    }

    private Booking createWaitingBooking() {
        return new Booking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item,
                booker,
                BookingStatus.WAITING
        );
    }

    private BookingCreateDto createBookingDto() {
        return new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
    }
}
