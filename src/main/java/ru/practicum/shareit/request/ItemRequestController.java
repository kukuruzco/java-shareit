package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> create(
            @Valid @RequestBody ItemRequestCreateDto createDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        ItemRequestDto createdRequest = itemRequestService.createRequest(userId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(
            @PathVariable Long requestId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getRequestById(userId, requestId);
    }
}