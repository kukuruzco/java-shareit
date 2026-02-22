package ru.practicum.shareit.comment;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public List<CommentDto> toDto(List<Comment> comments) {
        return comments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Comment toEntity(CommentCreateDto createDto, Item item, User author) {
        if (createDto == null) {
            return null;
        }

        return Comment.builder()
                .text(createDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
    }
}