package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + ownerId + " не найден"));
        Item item = ItemMapper.toItem(itemDto, ownerId);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new UserNotFoundException("Пользователь с id " + ownerId + " не найден");
        }
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));

        if (!item.getOwnerId().equals(ownerId)) {
            throw new ItemNotFoundException("Доступ запрещен: пользователь " + ownerId +
                    " не является владельцем вещи " + itemId);
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        String searchText = text.toLowerCase();
        return itemRepository.findAll().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item ->
                        item.getName().toLowerCase().contains(searchText) ||
                                item.getDescription().toLowerCase().contains(searchText)
                )
                .map(ItemMapper::toItemDto)
                .toList();
    }
}
