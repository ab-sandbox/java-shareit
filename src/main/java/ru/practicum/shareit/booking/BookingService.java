package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

public interface BookingService {

    BookingDto create(Long bookerId, BookingCreateDto dto);
}
