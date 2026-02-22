package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper {

    private final ItemMapper itemMapper;

    public ItemRequestDto toDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }

        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(Collections.emptyList())
                .build();
    }

    public ItemRequestDto toDtoWithItems(ItemRequest itemRequest, List<ru.practicum.shareit.item.Item> items) {
        if (itemRequest == null) {
            return null;
        }

        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(items.stream()
                        .map(itemMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public ItemRequest toEntity(ItemRequestCreateDto createDto, User requestor) {
        if (createDto == null) {
            return null;
        }

        return ItemRequest.builder()
                .description(createDto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
    }
}