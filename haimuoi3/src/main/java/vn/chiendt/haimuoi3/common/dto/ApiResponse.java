package vn.chiendt.haimuoi3.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @Builder.Default
    int code = 1000;
    String message;
    T result;

    // Static factory methods
    public static <T> ApiResponse<T> success(T result, String message) {
        return ApiResponse.<T>builder()
                .code(1000)
                .message(message)
                .result(result)
                .build();
    }
    public static <T> ApiResponse<T> success(T result) {
        return success(result, "Success");
    }
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
    public static <T> ApiResponse<T> error(String message) {
        return error(400, message);
    }
}

