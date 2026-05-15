package com.example.ai_engineering_enablement_portal.task.Exception;

import com.example.ai_engineering_enablement_portal.task.Api.ErrorResponse;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TaskNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.failure(exception.getMessage(), "TASK_NOT_FOUND"));
    }

    @ExceptionHandler(TaskConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(TaskConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.failure(exception.getMessage(), "CONFLICT"));
    }

    @ExceptionHandler(TaskDependencyException.class)
    public ResponseEntity<ErrorResponse> handleDependencyFailure(TaskDependencyException exception) {
        return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY)
                .body(ErrorResponse.failure(exception.getMessage(), "DEPENDENCY_FAILURE"));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.failure("Invalid request", "INVALID_INPUT",
                        Map.of("issue", exception.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.failure("Unexpected server error", "INTERNAL_SERVER_ERROR"));
    }
}
