package vn.chiendt.haimuoi3.user.service;

import vn.chiendt.haimuoi3.user.dto.request.ChangePasswordRequest;
import vn.chiendt.haimuoi3.user.dto.request.UpdateProfileRequest;
import vn.chiendt.haimuoi3.user.dto.response.CustomerProfileResponse;

public interface CustomerProfileService {

    CustomerProfileResponse getProfile(Long userId);

    CustomerProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);
}
