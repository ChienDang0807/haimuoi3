package vn.chiendt.haimuoi3.sysadmin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.service.UserManagementService;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.model.postgres.UserRole;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void suspend(Long userId) {
        UserEntity user = findById(userId);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activate(Long userId) {
        UserEntity user = findById(userId);
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserEntity> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public UserEntity createUser(CreateUserRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Check if phone already exists (if provided)
        if (request.getPhone() != null && !request.getPhone().isBlank() 
                && userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        // Create new user entity
        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.SHOP_OWNER) // Default role for sysadmin-created users
                .isActive(true)
                .isVerified(true) // Auto-verify sysadmin-created users
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public UserEntity updateUser(Long userId, UpdateUserRequest request) {
        UserEntity user = findById(userId);

        // Update fields if provided
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            // Check if phone is already used by another user
            if (userRepository.existsByPhone(request.getPhone())) {
                UserEntity existingUser = userRepository.findByPhone(request.getPhone())
                        .orElse(null);
                if (existingUser != null && !existingUser.getId().equals(userId)) {
                    throw new IllegalArgumentException("Phone number already exists");
                }
            }
            user.setPhone(request.getPhone());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }
}
