package ru.practicum.shareit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.*;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({ItemNotFoundException.class, UserNotFoundException.class, NotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN) // 403
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, DuplicateEmailException.class})
    public ResponseEntity<Map<String, String>> handleConflict(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)  // 409
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // 400
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleInternalError(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
                .body(Map.of("error", "Внутренняя ошибка сервера"));
    }

}