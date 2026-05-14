package vn.chiendt.haimuoi3.user.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.user.dto.request.ChangePasswordRequest;
import vn.chiendt.haimuoi3.user.dto.request.UpdateProfileRequest;
import vn.chiendt.haimuoi3.user.model.postgres.Gender;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ProfileValidator {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(Constants.User.PASSWORD_REGEX);

    public void validateUpdateProfile(UpdateProfileRequest request) {
        if (request.getGender() != null && !request.getGender().isBlank()) {
            try {
                Gender.valueOf(request.getGender().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid gender value. Must be MALE, FEMALE, or OTHER");
            }
        }
    }

    public void validateChangePassword(ChangePasswordRequest request) {
        if (request.getOldPassword() == null || request.getOldPassword().isBlank()) {
            throw new IllegalArgumentException("Old password is required");
        }

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }

        if (request.getNewPassword().length() < Constants.User.MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("New password must be at least " + Constants.User.MIN_PASSWORD_LENGTH + " characters");
        }

        if (request.getNewPassword().length() > Constants.User.MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("New password must not exceed " + Constants.User.MAX_PASSWORD_LENGTH + " characters");
        }

        if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            throw new IllegalArgumentException("New password must contain at least one letter and one number");
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException("New password must be different from old password");
        }
    }
}
