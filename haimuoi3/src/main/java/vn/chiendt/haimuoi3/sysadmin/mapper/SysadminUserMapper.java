package vn.chiendt.haimuoi3.sysadmin.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.response.UserResponse;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

@Mapper(componentModel = "spring")
public interface SysadminUserMapper {

    /**
     * Convert UserEntity to UserResponse DTO
     */
    @Mapping(target = "role", expression = "java(entity.getRole().name())")
    @Mapping(target = "status", expression = "java(entity.getIsActive() ? \"ACTIVE\" : \"INACTIVE\")")
    UserResponse toUserResponse(UserEntity entity);

    /**
     * Convert CreateUserRequest to UserEntity
     * Note: passwordHash and other fields should be set in the service layer
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toUserEntity(CreateUserRequest request);

    /**
     * Update UserEntity from UpdateUserRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserEntity(UpdateUserRequest request, @MappingTarget UserEntity entity);
}
