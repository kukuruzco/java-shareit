package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestCreateDto createDto) {
        log.info("Создание нового запроса вещи пользователем с id: {}", userId);

        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));

        ItemRequest itemRequest = itemRequestMapper.toEntity(createDto, requestor);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);

        log.info("Запрос вещи создан с id: {}", savedRequest.getId());
        return itemRequestMapper.toDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        log.info("Получение всех запросов пользователя с id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Пользователь с id " + userId + " не найден");
        }

        List<ItemRequest> userRequests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        // Получаем все вещи, созданные по этим запросам
        List<Long> requestIds = userRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<Item>> itemsByRequest = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return userRequests.stream()
                .map(request -> itemRequestMapper.toDtoWithItems(
                        request,
                        itemsByRequest.getOrDefault(request.getId(), List.of())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        log.info("Получение всех запросов вещей для пользователя с id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Пользователь с id " + userId + " не найден");
        }

        List<ItemRequest> allRequests = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId);

        // Получаем все вещи, созданные по этим запросам
        List<Long> requestIds = allRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<Item>> itemsByRequest = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return allRequests.stream()
                .map(request -> itemRequestMapper.toDtoWithItems(
                        request,
                        itemsByRequest.getOrDefault(request.getId(), List.of())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        log.info("Получение запроса вещи с id: {} для пользователя с id: {}", requestId, userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Пользователь с id " + userId + " не найден");
        }

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ItemRequestNotFoundException("Запрос вещи с id " + requestId + " не найден"));

        List<Item> items = itemRepository.findByRequestId(requestId);

        return itemRequestMapper.toDtoWithItems(itemRequest, items);
    }
}
