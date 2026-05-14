package vn.chiendt.haimuoi3.authentication.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.chiendt.haimuoi3.authentication.dto.response.AuthResponse;
import vn.chiendt.haimuoi3.authentication.model.postgres.RefreshTokenEntity;
import vn.chiendt.haimuoi3.authentication.service.RefreshTokenService;
import vn.chiendt.haimuoi3.common.config.JwtTokenProvider;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletRequest httpRequest) {
        
        RefreshTokenEntity tokenEntity = refreshTokenService.validateRefreshToken(refreshToken);
        
        UserEntity user = userRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Account is inactive");
        }
        
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        
        AuthResponse response = AuthResponse.builder()
                .accessToken(newAccessToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .build();
        
        return ApiResponse.success(response, "Token refreshed");
    }
}
