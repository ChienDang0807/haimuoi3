package vn.chiendt.haimuoi3.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.user.dto.request.ChangePasswordRequest;
import vn.chiendt.haimuoi3.user.dto.request.UpdateProfileRequest;
import vn.chiendt.haimuoi3.user.dto.response.CustomerProfileResponse;
import vn.chiendt.haimuoi3.user.mapper.CustomerProfileMapper;
import vn.chiendt.haimuoi3.user.model.postgres.CustomerProfileEntity;
import vn.chiendt.haimuoi3.user.model.postgres.Gender;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.repository.CustomerProfileRepository;
import vn.chiendt.haimuoi3.user.repository.UserRepository;
import vn.chiendt.haimuoi3.user.service.CustomerProfileService;
import vn.chiendt.haimuoi3.user.validator.ProfileValidator;

@Service
@RequiredArgsConstructor
public class CustomerProfileServiceImpl implements CustomerProfileService {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileValidator profileValidator;
    private final CustomerProfileMapper profileMapper;

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CustomerProfileEntity profile = customerProfileRepository.findByUserId(userId).orElse(null);

        return profileMapper.toResponse(user, profile);
    }

    @Override
    @Transactional
    public CustomerProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        profileValidator.validateUpdateProfile(request);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        userRepository.save(user);

        CustomerProfileEntity profile = customerProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    CustomerProfileEntity newProfile = new CustomerProfileEntity();
                    newProfile.setUserId(userId);
                    return newProfile;
                });

        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null && !request.getGender().isBlank()) {
            profile.setGender(Gender.valueOf(request.getGender().toUpperCase()));
        }
        customerProfileRepository.save(profile);

        return profileMapper.toResponse(user, profile);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        profileValidator.validateChangePassword(request);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
