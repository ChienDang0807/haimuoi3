package vn.chiendt.haimuoi3.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.chiendt.haimuoi3.common.constants.ApiErrorCode;
import vn.chiendt.haimuoi3.common.dto.ApiErrorResponse;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@Slf4j(topic = "GLOBAL-EXCEPTION")
public class GlobalException {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return toErrorResponse(ApiErrorCode.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ApiErrorResponse.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldViolation)
                .toList();
        String message = violations.isEmpty()
                ? ApiErrorCode.VALIDATION_ERROR.getMessage()
                : violations.get(0).getField() + ": " + violations.get(0).getMessage();
        return toErrorResponse(ApiErrorCode.VALIDATION_ERROR, message, request, violations);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return toErrorResponse(
                ApiErrorCode.BAD_REQUEST,
                "Malformed JSON or incompatible request body",
                request,
                null
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return toErrorResponse(ApiErrorCode.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error at {}", request.getRequestURI(), ex);
        return toErrorResponse(ApiErrorCode.INTERNAL_ERROR, null, request, null);
    }

    private ResponseEntity<ApiErrorResponse> toErrorResponse(
            ApiErrorCode errorCode,
            String message,
            HttpServletRequest request,
            List<ApiErrorResponse.FieldViolation> violations
    ) {
        String resolvedMessage = (message != null && !message.isBlank())
                ? message
                : errorCode.getMessage();
        ApiErrorResponse body = ApiErrorResponse.builder()
                .code(errorCode.getCode())
                .message(resolvedMessage)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .violations(violations)
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    private ApiErrorResponse.FieldViolation toFieldViolation(FieldError fe) {
        return ApiErrorResponse.FieldViolation.builder()
                .field(fe.getField())
                .message(fe.getDefaultMessage())
                .rejectedValue(fe.getRejectedValue())
                .build();
    }
}
