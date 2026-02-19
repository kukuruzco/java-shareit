package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        log.info("Создание новой вещи для пользователя с id: {}", ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + ownerId + " не найден"));

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
                .orElseThrow(() -> new ItemNotFoundException("Вещь с id " + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new SecurityException("Пользователь с id " + ownerId + " не является владельцем вещи");
        }

        itemMapper.updateEntity(itemDto, item);
        Item updatedItem = itemRepository.save(item);

        log.info("Вещь с id: {} обновлена", itemId);
        return itemMapper.toDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        log.info("Получение вещи с id: {}", itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с id " + itemId + " не найдена"));

        return itemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        log.info("Получение всех вещей пользователя с id: {}", ownerId);

        if (!userRepository.existsById(ownerId)) {
            throw new UserNotFoundException("Пользователь с id " + ownerId + " не найден");
        }

        return itemRepository.findByOwnerIdOrderByIdAsc(ownerId).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск вещей по тексту: {}", text);

        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return itemRepository.searchAvailableItems(text.trim()).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }
}