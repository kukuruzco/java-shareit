package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;

    @Override
    public List<CommentDto> getCommentsByItemId(Long itemId) {
        log.info("Получение комментариев для вещи {}", itemId);

        if (!itemRepository.existsById(itemId)) {
            throw new ItemNotFoundException("Вещь не найдена");
        }

        return commentMapper.toDto(commentRepository.findByItemId(itemId));
    }

    @Override
    public List<CommentDto> getCommentsByAuthorId(Long authorId) {
        log.info("Получение комментариев пользователя {}", authorId);

        if (!userRepository.existsById(authorId)) {
            throw new UserNotFoundException("Пользователь не найден");
        }

        return commentMapper.toDto(commentRepository.findByAuthorId(authorId));
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        log.info("Удаление комментария {} пользователем {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));

        if (!comment.getAuthor().getId().equals(userId) &&
                !comment.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Нет прав для удаления комментария");
        }

        commentRepository.deleteById(commentId);
        log.info("Комментарий {} удален", commentId);
    }
}