package com.ernoxin.codalapi.common.exception;

import com.ernoxin.codalapi.common.api.ApiResponse;
import com.ernoxin.codalapi.common.api.ErrorResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResult>> handleValidation(MethodArgumentNotValidException ex,
                                                                     HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());
        return buildError(HttpStatus.BAD_REQUEST, "Validation failed", errors, request, ex);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ErrorResult>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                       HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Invalid request parameter", List.of(ex.getMessage()), request, ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResult>> handleConstraintViolation(ConstraintViolationException ex,
                                                                              HttpServletRequest request) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());
        return buildError(HttpStatus.BAD_REQUEST, "Validation failed", errors, request, ex);
    }

    @ExceptionHandler(UpstreamApiException.class)
    public ResponseEntity<ApiResponse<ErrorResult>> handleUpstream(UpstreamApiException ex,
                                                                   HttpServletRequest request) {
        return buildError(ex.getStatus(), ex.getMessage(), List.of("Upstream CODAL API call failed"), request, ex);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<ErrorResult>> handleResponseStatus(ResponseStatusException ex,
                                                                         HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        String message = ex.getReason() != null && !ex.getReason().isBlank()
                ? ex.getReason()
                : "Request failed";
        return buildError(status != null ? status : HttpStatus.BAD_REQUEST, message, List.of(ex.getMessage()), request, ex);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResult>> handleNotFound(NoResourceFoundException ex,
                                                                   HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "Resource not found", List.of(ex.getMessage()), request, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResult>> handleUnexpected(Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", List.of(ex.getMessage()), request, ex);
    }

    private ResponseEntity<ApiResponse<ErrorResult>> buildError(HttpStatus status,
                                                                String message,
                                                                List<String> details,
                                                                HttpServletRequest request,
                                                                Exception ex) {
        log.error("Handled error status={} path={} message={}",
                status.value(), request.getRequestURI(), ex.getMessage(), ex);

        ErrorResult errorResult = new ErrorResult(details);
        ApiResponse<ErrorResult> body = ApiResponse.of(status, message, errorResult);
        return ResponseEntity.status(status).body(body);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
