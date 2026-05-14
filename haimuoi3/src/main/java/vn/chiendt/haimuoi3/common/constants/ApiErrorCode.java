package vn.chiendt.haimuoi3.common.constants;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Mã lỗi API thống nhất: mã nghiệp vụ, HTTP status mặc định, message mặc định (có thể ghi đè khi build response).
 */
@Getter
public enum ApiErrorCode {

    NOT_FOUND(1004, HttpStatus.NOT_FOUND, "Resource not found"),

    BAD_REQUEST(4000, HttpStatus.BAD_REQUEST, "Bad request"),

    VALIDATION_ERROR(4001, HttpStatus.BAD_REQUEST, "Validation failed"),

    USER_ALREADY_EXISTS(4090, HttpStatus.CONFLICT, "User with this email or phone already exists"),

    USER_NOT_FOUND(4041, HttpStatus.NOT_FOUND, "User not found"),

    INVALID_CREDENTIALS(4010, HttpStatus.UNAUTHORIZED, "Invalid email/phone or password"),

    UNAUTHORIZED_SHOP_ACCESS(4030, HttpStatus.FORBIDDEN, "You do not have permission to access this shop"),

    SHOP_NOT_FOUND(4042, HttpStatus.NOT_FOUND, "Shop not found"),

    INTERNAL_ERROR(5000, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    ApiErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
