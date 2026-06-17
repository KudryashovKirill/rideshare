package com.example.rideshare_rest.exception;
import com.example.rideshare_api_contract.dto.ErrorResponse;
import com.example.rideshare_api_contract.exceptions.NoFreeSeatsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.List;

/**
 * Централизованная обработка исключений.
 *
 * <p>Преобразует доменные исключения в ответы формата RFC 7807 Problem Details
 * ({@link ErrorResponse}). Это обеспечивает единообразный, машиночитаемый формат
 * ошибок для всех клиентов API.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE_PROBLEM_URI = "https://api.example.com/problems/";

    @ExceptionHandler(NoFreeSeatsException.class)
    public ResponseEntity<ErrorResponse> handleIsbnConflict(NoFreeSeatsException ex,
                                                            HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        BASE_PROBLEM_URI + "free-seats-conflict",
                        "Отсутствие свободных мест",
                        ex.getMessage(),
                        req.getRequestURI(),
                        Instant.now(),
                        null
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(
                        fe.getField(),
                        fe.getRejectedValue(),
                        fe.getDefaultMessage()
                ))
                .toList();

        String detail = fieldErrors.stream()
                .map(fe -> fe.field() + ": " + fe.message())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Ошибка валидации входных данных");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        BASE_PROBLEM_URI + "validation-error",
                        "Ошибка валидации",
                        detail,
                        req.getRequestURI(),
                        Instant.now(),
                        fieldErrors
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
        // Место для логирования: log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        BASE_PROBLEM_URI + "internal-error",
                        "Внутренняя ошибка сервера",
                        "Произошла непредвиденная ошибка. Обратитесь к поддержке.",
                        req.getRequestURI(),
                        Instant.now(),
                        null
                ));
    }
}
