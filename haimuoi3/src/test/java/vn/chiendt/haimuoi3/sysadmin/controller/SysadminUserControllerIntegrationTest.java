package vn.chiendt.haimuoi3.sysadmin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.chiendt.haimuoi3.common.config.JwtAuthenticationFilter;
import vn.chiendt.haimuoi3.common.config.JwtTokenProvider;
import vn.chiendt.haimuoi3.common.config.SecurityConfig;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.response.UserResponse;
import vn.chiendt.haimuoi3.sysadmin.mapper.SysadminUserMapper;
import vn.chiendt.haimuoi3.sysadmin.service.UserManagementService;
import vn.chiendt.haimuoi3.sysadmin.validator.SysadminUserValidator;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.model.postgres.UserRole;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SysadminUserController.class)
@Import({SecurityConfig.class, SysadminUserControllerIntegrationTest.TestSecurityBeans.class})
@TestPropertySource(properties = "spring.profiles.active=test")
class SysadminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserManagementService userManagementService;

    @MockitoBean
    private SysadminUserMapper userMapper;

    @MockitoBean
    private SysadminUserValidator userValidator;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void unauthenticatedRequests_areRejected() throws Exception {
        mockMvc.perform(get("/api/v1/sysadmin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerRequests_areForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/sysadmin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SHOP_OWNER")
    void shopOwnerRequests_areForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/sysadmin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturnUserList() throws Exception {
        // Given
        UserEntity user1 = createSampleUser(1L, "user1@example.com");
        UserEntity user2 = createSampleUser(2L, "user2@example.com");
        Page<UserEntity> userPage = new PageImpl<>(Arrays.asList(user1, user2));

        when(userManagementService.findAllUsers(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toUserResponse(user1)).thenReturn(createUserResponse(user1));
        when(userMapper.toUserResponse(user2)).thenReturn(createUserResponse(user2));

        // When/Then
        mockMvc.perform(get("/api/v1/sysadmin/users")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.users").isArray())
                .andExpect(jsonPath("$.result.currentPage").value(0))
                .andExpect(jsonPath("$.result.totalElements").value(2));

        verify(userManagementService).findAllUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_shouldReturnUser() throws Exception {
        // Given
        Long userId = 1L;
        UserEntity user = createSampleUser(userId, "test@example.com");

        when(userManagementService.findById(userId)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(createUserResponse(user));

        // When/Then
        mockMvc.perform(get("/api/v1/sysadmin/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(userId))
                .andExpect(jsonPath("$.result.email").value("test@example.com"));

        verify(userManagementService).findById(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldCreateNewUser() throws Exception {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("newuser@example.com")
                .fullName("New User")
                .phone("1234567890")
                .password("password123")
                .build();

        UserEntity createdUser = createSampleUser(1L, request.getEmail());

        doNothing().when(userValidator).validateCreateUserRequest(any());
        when(userManagementService.createUser(any(CreateUserRequest.class))).thenReturn(createdUser);
        when(userMapper.toUserResponse(createdUser)).thenReturn(createUserResponse(createdUser));

        // When/Then
        mockMvc.perform(post("/api/v1/sysadmin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.email").value(request.getEmail()))
                .andExpect(jsonPath("$.message").value("User created successfully"));

        verify(userValidator).validateCreateUserRequest(any());
        verify(userManagementService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_shouldUpdateExistingUser() throws Exception {
        // Given
        Long userId = 1L;
        UpdateUserRequest request = UpdateUserRequest.builder()
                .fullName("Updated Name")
                .phone("9876543210")
                .build();

        UserEntity updatedUser = createSampleUser(userId, "test@example.com");
        updatedUser.setFullName(request.getFullName());

        doNothing().when(userValidator).validateUpdateUserRequest(any());
        when(userManagementService.updateUser(anyLong(), any(UpdateUserRequest.class))).thenReturn(updatedUser);
        when(userMapper.toUserResponse(updatedUser)).thenReturn(createUserResponse(updatedUser));

        // When/Then
        mockMvc.perform(put("/api/v1/sysadmin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.fullName").value(request.getFullName()))
                .andExpect(jsonPath("$.message").value("User updated successfully"));

        verify(userValidator).validateUpdateUserRequest(any());
        verify(userManagementService).updateUser(anyLong(), any(UpdateUserRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void suspendUser_shouldSuspendUser() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(userManagementService).suspend(userId);

        // When/Then
        mockMvc.perform(put("/api/v1/sysadmin/users/{userId}/suspend", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("User suspended successfully"));

        verify(userManagementService).suspend(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateUser_shouldActivateUser() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(userManagementService).activate(userId);

        // When/Then
        mockMvc.perform(put("/api/v1/sysadmin/users/{userId}/activate", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("User activated successfully"));

        verify(userManagementService).activate(userId);
    }

    private UserEntity createSampleUser(Long id, String email) {
        return UserEntity.builder()
                .id(id)
                .email(email)
                .fullName("Test User")
                .phone("1234567890")
                .role(UserRole.SHOP_OWNER)
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private UserResponse createUserResponse(UserEntity entity) {
        return UserResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .phone(entity.getPhone())
                .role(entity.getRole().name())
                .status(entity.getIsActive() ? "ACTIVE" : "INACTIVE")
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @TestConfiguration
    static class TestSecurityBeans {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                                        UserRepository userRepository) {
            return new JwtAuthenticationFilter(jwtTokenProvider, userRepository);
        }
    }
}
