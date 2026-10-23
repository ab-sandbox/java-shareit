package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private Item item;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(
                new User(null, "Alexey", "alexey@example.com")
        );

        booker = userRepository.save(
                new User(null, "Ivan", "ivan@example.com")
        );

        item = itemRepository.save(
                new Item(
                        null,
                        "Дрель",
                        "Аккумуляторная дрель",
                        true,
                        owner,
                        null
                )
        );

        now = LocalDateTime.now();

        saveBooking(
                now.minusDays(3),
                now.minusDays(2),
                BookingStatus.APPROVED
        );

        saveBooking(
                now.minusHours(1),
                now.plusHours(1),
                BookingStatus.APPROVED
        );

        saveBooking(
                now.plusDays(1),
                now.plusDays(2),
                BookingStatus.WAITING
        );

        saveBooking(
                now.plusDays(3),
                now.plusDays(4),
                BookingStatus.REJECTED
        );
    }

    @Test
    void shouldFindAllBookingsByBooker() {
        List<Booking> bookings =
                bookingRepository.findByBooker(booker.getId());

        assertEquals(4, bookings.size());
        assertTrue(
                bookings.get(0).getStart()
                        .isAfter(bookings.get(1).getStart())
        );
    }

    @Test
    void shouldFindAllBookingsByOwner() {
        List<Booking> bookings =
                bookingRepository.findByOwner(owner.getId());

        assertEquals(4, bookings.size());
    }

    @Test
    void shouldFindCurrentBookings() {
        List<Booking> bookings =
                bookingRepository.findCurrentByBooker(
                        booker.getId(), now
                );

        assertEquals(1, bookings.size());
        assertEquals(
                BookingStatus.APPROVED,
                bookings.get(0).getStatus()
        );
    }

    @Test
    void shouldFindPastBookings() {
        List<Booking> bookings =
                bookingRepository.findPastByBooker(
                        booker.getId(), now
                );

        assertEquals(1, bookings.size());
    }

    @Test
    void shouldFindFutureBookings() {
        List<Booking> bookings =
                bookingRepository.findFutureByBooker(
                        booker.getId(), now
                );

        assertEquals(2, bookings.size());
    }

    @Test
    void shouldFindWaitingBookings() {
        List<Booking> bookings =
                bookingRepository.findByBookerAndStatus(
                        booker.getId(),
                        BookingStatus.WAITING
                );

        assertEquals(1, bookings.size());
    }

    @Test
    void shouldFindRejectedBookings() {
        List<Booking> bookings =
                bookingRepository.findByBookerAndStatus(
                        booker.getId(),
                        BookingStatus.REJECTED
                );

        assertEquals(1, bookings.size());
    }

    @Test
    void shouldFindFutureBookingsByOwner() {
        List<Booking> bookings =
                bookingRepository.findFutureByOwner(
                        owner.getId(), now
                );

        assertEquals(2, bookings.size());
    }

    @Test
    void shouldFindCurrentBookingsByOwner() {
        List<Booking> bookings =
                bookingRepository.findCurrentByOwner(
                        owner.getId(), now
                );

        assertEquals(1, bookings.size());
    }

    @Test
    void shouldFindPastBookingsByOwner() {
        List<Booking> bookings =
                bookingRepository.findPastByOwner(
                        owner.getId(), now
                );

        assertEquals(1, bookings.size());
    }

    @Test
    void shouldFindBookingsByOwnerAndStatus() {
        List<Booking> bookings =
                bookingRepository.findByOwnerAndStatus(
                        owner.getId(),
                        BookingStatus.WAITING
                );

        assertEquals(1, bookings.size());
    }

    private void saveBooking(
            LocalDateTime start,
            LocalDateTime end,
            BookingStatus status) {

        bookingRepository.save(
                new Booking(
                        null,
                        start,
                        end,
                        item,
                        booker,
                        status
                )
        );
    }
}
