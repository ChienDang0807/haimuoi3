package vn.chiendt.haimuoi3.sysadmin.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.chiendt.haimuoi3.shop.model.postgres.ShopEntity;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

@Mapper(componentModel = "spring")
public interface SysadminShopMapper {

    /**
     * Convert ShopEntity to ShopResponse DTO with owner information
     */
    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.shopName", target = "shopName")
    @Mapping(source = "entity.slug", target = "slug")
    @Mapping(source = "entity.ownerId", target = "ownerId")
    @Mapping(source = "entity.description", target = "description")
    @Mapping(source = "entity.logoUrl", target = "logoUrl")
    @Mapping(source = "entity.bannerUrl", target = "bannerUrl")
    @Mapping(source = "entity.email", target = "email")
    @Mapping(source = "entity.phone", target = "phone")
    @Mapping(source = "entity.province", target = "province")
    @Mapping(source = "entity.district", target = "district")
    @Mapping(source = "entity.addressDetail", target = "addressDetail")
    @Mapping(source = "entity.createdAt", target = "createdAt")
    @Mapping(source = "entity.updatedAt", target = "updatedAt")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    @Mapping(target = "ownerName", expression = "java(owner != null ? owner.getFullName() : null)")
    @Mapping(target = "ownerEmail", expression = "java(owner != null ? owner.getEmail() : null)")
    ShopResponse toShopResponse(ShopEntity entity, UserEntity owner);

    /**
     * Convert CreateShopRequest to ShopEntity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "logoUrl", ignore = true)
    @Mapping(target = "bannerUrl", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ShopEntity toShopEntity(CreateShopRequest request);

    /**
     * Update ShopEntity from UpdateShopRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "logoUrl", ignore = true)
    @Mapping(target = "bannerUrl", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateShopEntity(UpdateShopRequest request, @MappingTarget ShopEntity entity);
}
