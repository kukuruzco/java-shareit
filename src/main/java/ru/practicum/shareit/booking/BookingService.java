package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(Long userId, BookingCreateDto createDto);

    BookingDto approveBooking(Long userId, Long bookingId, Boolean approved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getUserBookings(Long userId, String state);

    List<BookingDto> getOwnerBookings(Long userId, String state);
}