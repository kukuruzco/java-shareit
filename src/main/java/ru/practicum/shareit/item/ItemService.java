package ru.practicum.shareit.item;

import ru.practicum.shareit.comment.CommentCreateDto;
import ru.practicum.shareit.comment.CommentDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemDto getItemById(Long itemId, Long userId);

    List<ItemDto> getItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text);

    CommentDto addComment(Long userId, Long itemId, CommentCreateDto commentCreateDto);
}