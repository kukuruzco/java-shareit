package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingInfo;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.comment.*;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final CommentService commentService;

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        log.info("Создание новой вещи для пользователя с id: {}", ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElse(null);
        }

        Item item = itemMapper.toEntity(itemDto, owner, request);
        Item savedItem = itemRepository.save(item);

        log.info("Вещь создана с id: {}", savedItem.getId());
        return itemMapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        log.info("Обновление вещи с id: {} для пользователя с id: {}", itemId, ownerId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Пользователь не является владельцем вещи");
        }

        itemMapper.updateEntity(itemDto, item);
        Item updatedItem = itemRepository.save(item);

        log.info("Вещь с id: {} обновлена", itemId);
        return getItemById(itemId, ownerId);
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        log.info("Получение вещи с id: {} пользователем с id: {}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));

        ItemDto itemDto = itemMapper.toDto(item);

        List<Comment> comments = commentRepository.findByItemId(itemId);
        itemDto.setComments(commentMapper.toDto(comments));

        if (item.getOwner().getId().equals(userId)) {
            addBookingInfoToDto(itemDto, item.getId());
        }

        return itemDto;
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        log.info("Получение всех вещей пользователя с id: {}", ownerId);

        if (!userRepository.existsById(ownerId)) {
            throw new UserNotFoundException("Пользователь не найден");
        }

        List<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(ownerId);
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        Map<Long, List<Booking>> bookingsByItem = getBookingsForItems(itemIds);

        Map<Long, List<Comment>> commentsByItem = getCommentsForItems(itemIds);

        return items.stream()
                .map(item -> {
                    ItemDto dto = itemMapper.toDto(item);

                    dto.setComments(commentMapper.toDto(
                            commentsByItem.getOrDefault(item.getId(), new ArrayList<>())
                    ));

                    List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), new ArrayList<>());
                    addBookingInfoToDto(dto, itemBookings);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск вещей по тексту: {}", text);

        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<Item> items = itemRepository.searchAvailableItems(text.trim());

        return items.stream()
                .map(item -> {
                    ItemDto dto = itemMapper.toDto(item);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto createDto) {
        log.info("Создание комментария пользователем {} для вещи {}", userId, itemId);

        try {
            User author = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
            log.debug("Автор найден: {}", author.getId());

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));
            log.debug("Вещь найдена: {}", item.getId());

            boolean hasBooked = bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                    itemId, userId, BookingStatus.APPROVED, LocalDateTime.now());
            log.debug("Проверка бронирования: {}", hasBooked);

            if (!hasBooked) {
                throw new BadRequestException("Нельзя оставить комментарий к вещи, которую вы не бронировали");
            }

            Comment comment = commentMapper.toEntity(createDto, item, author);
            log.debug("Комментарий создан: {}", comment);

            Comment savedComment = commentRepository.save(comment);
            log.info("Комментарий создан с id: {}", savedComment.getId());

            CommentDto result = commentMapper.toDto(savedComment);
            log.debug("Результат DTO: {}", result);

            return result;
        } catch (Exception e) {
            log.error("Ошибка при создании комментария: ", e);
            throw e;
        }
    }

    private Map<Long, List<Booking>> getBookingsForItems(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Booking> allBookings = bookingRepository.findAllByItemIds(itemIds);
        return allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));
    }

    private Map<Long, List<Comment>> getCommentsForItems(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Comment> allComments = commentRepository.findByItemIdIn(itemIds);
        return allComments.stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));
    }

    private void addBookingInfoToDto(ItemDto dto, Long itemId) {
        List<Booking> bookings = bookingRepository.findByItemId(itemId);
        addBookingInfoToDto(dto, bookings);
    }

    private void addBookingInfoToDto(ItemDto dto, List<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();

        Optional<Booking> lastBooking = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .filter(b -> !b.getStart().isAfter(now))
                .max(Comparator.comparing(Booking::getStart));

        lastBooking.ifPresent(booking ->
                dto.setLastBooking(convertToBookingInfo(booking))
        );

        Optional<Booking> nextBooking = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart));

        nextBooking.ifPresent(booking ->
                dto.setNextBooking(convertToBookingInfo(booking))
        );
    }

    private BookingInfo convertToBookingInfo(Booking booking) {
        return BookingInfo.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }
}