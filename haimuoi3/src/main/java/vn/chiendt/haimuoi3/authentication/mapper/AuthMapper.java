package vn.chiendt.haimuoi3.authentication.mapper;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.authentication.dto.response.AuthResponse;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

@Component
public class AuthMapper {

    public AuthResponse toAuthResponse(UserEntity user, String token, Long expiresIn) {
        return AuthResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(expiresIn)
                .build();
    }
}
