package vn.chiendt.haimuoi3.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.user.dto.request.ChangePasswordRequest;
import vn.chiendt.haimuoi3.user.dto.request.UpdateProfileRequest;
import vn.chiendt.haimuoi3.user.dto.response.CustomerProfileResponse;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.service.CustomerProfileService;

@RestController
@RequestMapping("/api/v1/customers/me")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerProfileService profileService;

    @GetMapping("/profile")
    public ApiResponse<CustomerProfileResponse> getProfile(
            @AuthenticationPrincipal UserEntity currentUser) {
        CustomerProfileResponse response = profileService.getProfile(currentUser.getId());
        return ApiResponse.success(response, "Profile retrieved successfully");
    }

    @PutMapping("/profile")
    public ApiResponse<CustomerProfileResponse> updateProfile(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestBody UpdateProfileRequest request) {
        CustomerProfileResponse response = profileService.updateProfile(currentUser.getId(), request);
        return ApiResponse.success(response, "Profile updated successfully");
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(currentUser.getId(), request);
        return ApiResponse.success(null, "Password changed successfully");
    }
}
