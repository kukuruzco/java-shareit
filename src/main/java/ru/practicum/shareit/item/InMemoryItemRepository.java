package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> storage = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(counter.getAndIncrement());
        }
        storage.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        List<Item> result = new ArrayList<>();
        for (Item item : storage.values()) {
            if (ownerId.equals(item.getOwnerId())) {
                result.add(item);
            }
        }
        result.sort(Comparator.comparing(Item::getId));
        return result;
    }
}
