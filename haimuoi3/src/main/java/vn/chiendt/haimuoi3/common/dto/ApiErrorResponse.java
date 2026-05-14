package vn.chiendt.haimuoi3.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    /** Mã lỗi nghiệp vụ — nên dùng {@link vn.chiendt.haimuoi3.common.constants.ApiErrorCode}. */
    int code;

    String message;

    Instant timestamp;

    /** URI request gây lỗi (vd: /api/v1/global-categories/xyz). */
    String path;

    /** Chi tiết lỗi từng field khi validate (Bean Validation). */
    List<FieldViolation> violations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldViolation {
        String field;
        String message;
        Object rejectedValue;
    }
}
