package vn.chiendt.haimuoi3.sysadmin.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.response.UserResponse;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.model.postgres.UserRole;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SysadminUserMapperTest {

    private final SysadminUserMapper mapper = Mappers.getMapper(SysadminUserMapper.class);

    @Test
    void toUserResponse_shouldMapEntityToResponse() {
        // Given
        UserEntity entity = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .phone("1234567890")
                .role(UserRole.SHOP_OWNER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        UserResponse response = mapper.toUserResponse(entity);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(entity.getId());
        assertThat(response.getEmail()).isEqualTo(entity.getEmail());
        assertThat(response.getFullName()).isEqualTo(entity.getFullName());
        assertThat(response.getPhone()).isEqualTo(entity.getPhone());
        assertThat(response.getRole()).isEqualTo("SHOP_OWNER");
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getCreatedAt()).isEqualTo(entity.getCreatedAt());
    }

    @Test
    void toUserResponse_shouldMapInactiveStatus() {
        // Given
        UserEntity entity = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.CUSTOMER)
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        UserResponse response = mapper.toUserResponse(entity);

        // Then
        assertThat(response.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    void toUserEntity_shouldMapRequestToEntity() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("newuser@example.com")
                .fullName("New User")
                .phone("1234567890")
                .password("password123")
                .build();

        // When
        UserEntity entity = mapper.toUserEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getEmail()).isEqualTo(request.getEmail());
        assertThat(entity.getFullName()).isEqualTo(request.getFullName());
        assertThat(entity.getPhone()).isEqualTo(request.getPhone());
        // Password should be ignored by mapper (handled in service)
        assertThat(entity.getPasswordHash()).isNull();
    }

    @Test
    void updateUserEntity_shouldUpdateOnlyProvidedFields() {
        // Given
        UserEntity existingEntity = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Old Name")
                .phone("1234567890")
                .role(UserRole.SHOP_OWNER)
                .isActive(true)
                .build();

        UpdateUserRequest request = UpdateUserRequest.builder()
                .fullName("Updated Name")
                .phone("9876543210")
                .build();

        // When
        mapper.updateUserEntity(request, existingEntity);

        // Then
        assertThat(existingEntity.getFullName()).isEqualTo(request.getFullName());
        assertThat(existingEntity.getPhone()).isEqualTo(request.getPhone());
        // These should remain unchanged
        assertThat(existingEntity.getId()).isEqualTo(1L);
        assertThat(existingEntity.getEmail()).isEqualTo("test@example.com");
        assertThat(existingEntity.getRole()).isEqualTo(UserRole.SHOP_OWNER);
        assertThat(existingEntity.getIsActive()).isTrue();
    }
}
