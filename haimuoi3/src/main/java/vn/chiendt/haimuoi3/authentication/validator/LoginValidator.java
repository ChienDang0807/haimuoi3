package vn.chiendt.haimuoi3.authentication.validator;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.authentication.dto.request.LoginRequest;

@Component
public class LoginValidator {

    public void validate(LoginRequest request) {
        if (request.getEmailOrPhone() == null || request.getEmailOrPhone().isBlank()) {
            throw new IllegalArgumentException("Email or phone is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
