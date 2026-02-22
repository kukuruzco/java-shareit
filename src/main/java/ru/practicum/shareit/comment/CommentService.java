package ru.practicum.shareit.comment;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId, Long itemId, CommentCreateDto createDto);

    List<CommentDto> getCommentsByItemId(Long itemId);

    List<CommentDto> getCommentsByAuthorId(Long authorId);

    void deleteComment(Long commentId, Long userId);
}
