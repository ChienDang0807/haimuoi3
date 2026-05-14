package vn.chiendt.haimuoi3.authentication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String email;
    private String phone;
    private String password;
    private String passwordConfirm;
    private String fullName;
}
