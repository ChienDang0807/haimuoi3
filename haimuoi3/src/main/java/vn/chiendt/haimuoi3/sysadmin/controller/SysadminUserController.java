package vn.chiendt.haimuoi3.sysadmin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.response.UserListResponse;
import vn.chiendt.haimuoi3.sysadmin.dto.response.UserResponse;
import vn.chiendt.haimuoi3.sysadmin.mapper.SysadminUserMapper;
import vn.chiendt.haimuoi3.sysadmin.service.UserManagementService;
import vn.chiendt.haimuoi3.sysadmin.validator.SysadminUserValidator;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sysadmin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SysadminUserController {

    private final UserManagementService userManagementService;
    private final SysadminUserMapper userMapper;
    private final SysadminUserValidator userValidator;

    /**
     * GET /api/v1/sysadmin/users - List all users with pagination
     */
    @GetMapping
    public ApiResponse<UserListResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<UserEntity> userPage = userManagementService.findAllUsers(pageable);
        
        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());

        UserListResponse response = UserListResponse.builder()
                .users(userResponses)
                .currentPage(userPage.getNumber())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .pageSize(userPage.getSize())
                .build();

        return ApiResponse.success(response, "Users retrieved successfully");
    }

    /**
     * GET /api/v1/sysadmin/users/{userId} - Get user detail
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long userId) {
        UserEntity user = userManagementService.findById(userId);
        UserResponse response = userMapper.toUserResponse(user);
        return ApiResponse.success(response, "User retrieved successfully");
    }

    /**
     * POST /api/v1/sysadmin/users - Create new user
     */
    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        userValidator.validateCreateUserRequest(request);
        UserEntity user = userManagementService.createUser(request);
        UserResponse response = userMapper.toUserResponse(user);
        return ApiResponse.success(response, "User created successfully");
    }

    /**
     * PUT /api/v1/sysadmin/users/{userId} - Update user
     */
    @PutMapping("/{userId}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request) {
        userValidator.validateUpdateUserRequest(request);
        UserEntity user = userManagementService.updateUser(userId, request);
        UserResponse response = userMapper.toUserResponse(user);
        return ApiResponse.success(response, "User updated successfully");
    }

    /**
     * PUT /api/v1/sysadmin/users/{userId}/suspend - Suspend user
     */
    @PutMapping("/{userId}/suspend")
    public ApiResponse<Void> suspendUser(@PathVariable Long userId) {
        userManagementService.suspend(userId);
        return ApiResponse.success(null, "User suspended successfully");
    }

    /**
     * PUT /api/v1/sysadmin/users/{userId}/activate - Activate user
     */
    @PutMapping("/{userId}/activate")
    public ApiResponse<Void> activateUser(@PathVariable Long userId) {
        userManagementService.activate(userId);
        return ApiResponse.success(null, "User activated successfully");
    }
}
