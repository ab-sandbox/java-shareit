package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    BookingDto create(Long bookerId, BookingCreateDto dto);

    BookingDto approve(Long ownerId, Long bookingId, boolean approved);

    List<BookingDto> findByBooker(Long bookerId, BookingState state);

    List<BookingDto> findByOwner(Long ownerId, BookingState state);
}
