package vn.chiendt.haimuoi3.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfileResponse {
    private Long userId;
    private String email;
    private String phone;
    private String fullName;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private String gender;
    private Boolean isVerified;
}
