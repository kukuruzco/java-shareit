package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.CommentCreateDto;
import ru.practicum.shareit.comment.CommentDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> create(
            @Valid @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        ItemDto createdItem = itemService.createItem(itemDto, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @GetMapping("/{itemId}")
    public ItemDto get(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getItemsByOwner(ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.updateItem(itemId, itemDto, ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestParam String text,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long itemId,
            @Valid @RequestBody CommentCreateDto commentCreateDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        CommentDto comment = itemService.addComment(userId, itemId, commentCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
}