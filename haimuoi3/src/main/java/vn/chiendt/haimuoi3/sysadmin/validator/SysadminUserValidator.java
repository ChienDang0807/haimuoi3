package vn.chiendt.haimuoi3.sysadmin.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateUserRequest;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SysadminUserValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(Constants.User.PASSWORD_REGEX);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");

    /**
     * Validate CreateUserRequest
     */
    public void validateCreateUserRequest(CreateUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create user request cannot be null");
        }

        // Validate email
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new IllegalArgumentException("Email format is invalid");
        }

        // Validate full name
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (request.getFullName().length() > 100) {
            throw new IllegalArgumentException("Full name must not exceed 100 characters");
        }

        // Validate phone (optional but must be valid if provided)
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (!PHONE_PATTERN.matcher(request.getPhone()).matches()) {
                throw new IllegalArgumentException("Phone number must be 10-15 digits");
            }
        }

        // Validate password
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
    }

    /**
     * Validate UpdateUserRequest
     */
    public void validateUpdateUserRequest(UpdateUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Update user request cannot be null");
        }

        // Validate full name if provided
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            if (request.getFullName().length() > 100) {
                throw new IllegalArgumentException("Full name must not exceed 100 characters");
            }
        }

        // Validate phone if provided
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (!PHONE_PATTERN.matcher(request.getPhone()).matches()) {
                throw new IllegalArgumentException("Phone number must be 10-15 digits");
            }
        }
    }
}
