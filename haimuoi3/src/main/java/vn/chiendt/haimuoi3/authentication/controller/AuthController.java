package vn.chiendt.haimuoi3.authentication.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.chiendt.haimuoi3.authentication.dto.request.LoginRequest;
import vn.chiendt.haimuoi3.authentication.dto.request.RegisterRequest;
import vn.chiendt.haimuoi3.authentication.dto.response.AuthResponse;
import vn.chiendt.haimuoi3.authentication.dto.response.UserInfoResponse;
import vn.chiendt.haimuoi3.authentication.model.postgres.RefreshTokenEntity;
import vn.chiendt.haimuoi3.authentication.service.AuthService;
import vn.chiendt.haimuoi3.authentication.service.RefreshTokenService;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AuthResponse response = authService.register(request);

        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(
                response.getUserId(),
                httpRequest.getHeader("User-Agent"),
                httpRequest.getRemoteAddr()
        );

        setRefreshTokenCookie(httpResponse, refreshToken.getToken());

        return ApiResponse.success(response, "User registered successfully");
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AuthResponse response = authService.login(request);

        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(
                response.getUserId(),
                httpRequest.getHeader("User-Agent"),
                httpRequest.getRemoteAddr()
        );

        setRefreshTokenCookie(httpResponse, refreshToken.getToken());

        return ApiResponse.success(response, "Login successful");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken != null) {
            refreshTokenService.deleteRefreshToken(refreshToken);
        }

        clearRefreshTokenCookie(response);

        return ApiResponse.success(null, "Logout successful");
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getCurrentUser(@AuthenticationPrincipal UserEntity user) {
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();

        return ApiResponse.success(userInfo, "User info retrieved successfully");
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(90 * 24 * 60 * 60);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
