package ru.practicum.shareit.item;

import java.util.List;

public interface CommentService {

    List<CommentDto> getCommentsByItemId(Long itemId);

    List<CommentDto> getCommentsByAuthorId(Long authorId);

    void deleteComment(Long commentId, Long userId);
}
