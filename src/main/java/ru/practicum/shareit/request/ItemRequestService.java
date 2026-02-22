package ru.practicum.shareit.request;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto createRequest(Long userId, ItemRequestCreateDto createDto);

    List<ItemRequestDto> getUserRequests(Long userId);

    List<ItemRequestDto> getAllRequests(Long userId);

    ItemRequestDto getRequestById(Long userId, Long requestId);
}