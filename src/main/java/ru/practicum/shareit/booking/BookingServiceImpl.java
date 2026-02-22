package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingCreateDto createDto) {
        log.info("Создание бронирования пользователем {} для вещи {}", userId, createDto.getItemId());

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(createDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));

        if (item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }

        if (!createDto.getEnd().isAfter(createDto.getStart())) {
            throw new BadRequestException("Дата окончания должна быть позже даты начала");
        }

        Booking booking = bookingMapper.toEntity(createDto, item, booker);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Бронирование создано с id: {}", savedBooking.getId());
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        log.info("Подтверждение бронирования {} пользователем {} со статусом {}",
                bookingId, userId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Только владелец вещи может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Бронирование {} обновлено, статус: {}", bookingId, updatedBooking.getStatus());

        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        log.info("Получение бронирования {} пользователем {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Нет прав для просмотра этого бронирования");
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state) {
        log.info("Получение бронирований пользователя {} со статусом {}", userId, state);

        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        BookingState bookingState = parseState(state);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findByBookerId(userId, SORT_BY_START_DESC);
            case CURRENT -> bookingRepository.findCurrentByBookerId(userId, now);
            case PAST -> bookingRepository.findByBookerIdAndEndIsBefore(userId, now, SORT_BY_START_DESC);
            case FUTURE -> bookingRepository.findByBookerIdAndStartIsAfter(userId, now, SORT_BY_START_DESC);
            case WAITING ->
                    bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, SORT_BY_START_DESC);
            case REJECTED ->
                    bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
            default -> throw new BadRequestException("Unknown state: " + state);
        };

        return bookings.stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String state) {
        log.info("Получение бронирований для вещей владельца {} со статусом {}", userId, state);

        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        BookingState bookingState = parseState(state);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerId(userId, SORT_BY_START_DESC);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentByItemOwnerId(userId, now);
                break;
            case PAST:
                bookings = bookingRepository.findPastByItemOwnerId(userId, now, SORT_BY_START_DESC);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureByItemOwnerId(userId, now, SORT_BY_START_DESC);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, SORT_BY_START_DESC);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                break;
            default:
                throw new BadRequestException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    private BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }
}