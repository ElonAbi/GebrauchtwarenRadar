package de.kleinanzeigen.app.shared;

import de.kleinanzeigen.app.searchprofile.DuplicateSearchProfileException;
import de.kleinanzeigen.app.searchprofile.SearchProfileNotFoundException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(SearchProfileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(SearchProfileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problem("not-found", ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(DuplicateSearchProfileException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateSearchProfileException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problem("duplicate", ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest()
                .body(problem("validation-error", ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(problem("invalid-argument", ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    private Map<String, Object> problem(String code, String detail, HttpStatus status) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "code", code,
                "detail", detail
        );
    }
}
