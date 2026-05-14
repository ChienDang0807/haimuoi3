package vn.chiendt.haimuoi3.sysadmin.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.service.impl.UserManagementServiceImpl;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.model.postgres.UserRole;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserManagementServiceImpl userManagementService;

    @Test
    void suspend_shouldSetIsActiveFalse() {
        // Given
        Long userId = 1L;
        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("test@example.com")
                .isActive(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        // When
        userManagementService.suspend(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).save(argThat(u -> !u.getIsActive()));
    }

    @Test
    void activate_shouldSetIsActiveTrue() {
        // Given
        Long userId = 1L;
        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("test@example.com")
                .isActive(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        // When
        userManagementService.activate(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).save(argThat(u -> u.getIsActive()));
    }

    @Test
    void findAllUsers_shouldReturnPagedUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        UserEntity user1 = UserEntity.builder().id(1L).email("user1@example.com").build();
        UserEntity user2 = UserEntity.builder().id(2L).email("user2@example.com").build();
        Page<UserEntity> userPage = new PageImpl<>(Arrays.asList(user1, user2));

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserEntity> result = userManagementService.findAllUsers(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(userRepository).findAll(pageable);
    }

    @Test
    void createUser_shouldCreateNewUser() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("newuser@example.com")
                .fullName("New User")
                .phone("1234567890")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserEntity result = userManagementService.createUser(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(request.getEmail());
        assertThat(result.getFullName()).isEqualTo(request.getFullName());
        assertThat(result.getRole()).isEqualTo(UserRole.SHOP_OWNER);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getIsVerified()).isTrue();
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void createUser_shouldThrowExceptionWhenEmailExists() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("existing@example.com")
                .fullName("New User")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userManagementService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void createUser_shouldThrowExceptionWhenPhoneExists() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("newuser@example.com")
                .fullName("New User")
                .phone("1234567890")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userManagementService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number already exists");

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateUser_shouldUpdateUserFields() {
        // Given
        Long userId = 1L;
        UpdateUserRequest request = UpdateUserRequest.builder()
                .fullName("Updated Name")
                .phone("9876543210")
                .build();

        UserEntity existingUser = UserEntity.builder()
                .id(userId)
                .email("test@example.com")
                .fullName("Old Name")
                .phone("1234567890")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserEntity result = userManagementService.updateUser(userId, request);

        // Then
        assertThat(result.getFullName()).isEqualTo(request.getFullName());
        assertThat(result.getPhone()).isEqualTo(request.getPhone());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void findById_shouldReturnUser() {
        // Given
        Long userId = 1L;
        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("test@example.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        UserEntity result = userManagementService.findById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    void findById_shouldThrowExceptionWhenUserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userManagementService.findById(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }
}
