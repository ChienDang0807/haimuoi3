package vn.chiendt.haimuoi3.user.mapper;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.user.dto.response.CustomerProfileResponse;
import vn.chiendt.haimuoi3.user.model.postgres.CustomerProfileEntity;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

@Component
public class CustomerProfileMapper {

    public CustomerProfileResponse toResponse(UserEntity user, CustomerProfileEntity profile) {
        return CustomerProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .dateOfBirth(profile != null ? profile.getDateOfBirth() : null)
                .gender(profile != null && profile.getGender() != null ? profile.getGender().name() : null)
                .isVerified(user.getIsVerified())
                .build();
    }
}
