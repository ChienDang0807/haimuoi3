package vn.chiendt.haimuoi3.authentication.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.authentication.dto.request.RegisterRequest;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RegisterRequestValidator {

    private final UserRepository userRepository;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(Constants.User.PASSWORD_REGEX);

    public void validate(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (request.getPassword().length() < Constants.User.MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + Constants.User.MIN_PASSWORD_LENGTH + " characters");
        }

        if (request.getPassword().length() > Constants.User.MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must not exceed " + Constants.User.MAX_PASSWORD_LENGTH + " characters");
        }

        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new IllegalArgumentException("Password must contain at least one letter and one number");
        }

        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("Password and password confirmation do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank() 
            && userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists");
        }
    }
}
