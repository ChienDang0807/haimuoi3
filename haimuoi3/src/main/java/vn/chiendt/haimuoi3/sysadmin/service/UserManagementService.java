package vn.chiendt.haimuoi3.sysadmin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateUserRequest;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

public interface UserManagementService {

    /**
     * Suspend a user account (set isActive=false)
     * @param userId the user ID to suspend
     */
    void suspend(Long userId);

    /**
     * Activate a user account (set isActive=true)
     * @param userId the user ID to activate
     */
    void activate(Long userId);

    /**
     * Find all users with optional filtering and pagination
     * @param pageable pagination parameters
     * @return page of users
     */
    Page<UserEntity> findAllUsers(Pageable pageable);

    /**
     * Create a new user
     * @param request the create user request
     * @return the created user entity
     */
    UserEntity createUser(CreateUserRequest request);

    /**
     * Update an existing user
     * @param userId the user ID to update
     * @param request the update user request
     * @return the updated user entity
     */
    UserEntity updateUser(Long userId, UpdateUserRequest request);

    /**
     * Find a user by ID
     * @param userId the user ID
     * @return the user entity
     */
    UserEntity findById(Long userId);
}
