package vn.chiendt.haimuoi3.sysadmin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    private String email;
    private String fullName;
    private String phone;
    private String password;
}
