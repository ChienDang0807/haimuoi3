package vn.chiendt.haimuoi3.sysadmin.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.chiendt.haimuoi3.shop.model.postgres.ShopEntity;
import vn.chiendt.haimuoi3.shop.model.postgres.ShopStatus;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.model.postgres.UserRole;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SysadminShopMapperTest {

    private final SysadminShopMapper mapper = Mappers.getMapper(SysadminShopMapper.class);

    @Test
    void toShopResponse_WithOwner_ShouldMapCorrectly() {
        // Given
        UserEntity owner = UserEntity.builder()
                .id(1L)
                .email("owner@example.com")
                .fullName("John Doe")
                .role(UserRole.SHOP_OWNER)
                .build();

        ShopEntity entity = ShopEntity.builder()
                .id(1L)
                .ownerId(1L)
                .shopName("Test Shop")
                .slug("test-shop")
                .description("Test description")
                .email("shop@example.com")
                .phone("0123456789")
                .province("Hanoi")
                .district("Cau Giay")
                .addressDetail("123 Test Street")
                .status(ShopStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        ShopResponse response = mapper.toShopResponse(entity, owner);

        // Then
        assertNotNull(response);
        assertEquals(entity.getId(), response.getId());
        assertEquals(entity.getShopName(), response.getShopName());
        assertEquals(entity.getSlug(), response.getSlug());
        assertEquals(entity.getOwnerId(), response.getOwnerId());
        assertEquals(owner.getFullName(), response.getOwnerName());
        assertEquals(owner.getEmail(), response.getOwnerEmail());
        assertEquals(entity.getDescription(), response.getDescription());
        assertEquals(entity.getEmail(), response.getEmail());
        assertEquals(entity.getPhone(), response.getPhone());
        assertEquals(entity.getProvince(), response.getProvince());
        assertEquals(entity.getDistrict(), response.getDistrict());
        assertEquals(entity.getAddressDetail(), response.getAddressDetail());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals(entity.getCreatedAt(), response.getCreatedAt());
        assertEquals(entity.getUpdatedAt(), response.getUpdatedAt());
    }

    @Test
    void toShopResponse_WithoutOwner_ShouldMapCorrectly() {
        // Given
        ShopEntity entity = ShopEntity.builder()
                .id(1L)
                .shopName("Test Shop")
                .slug("test-shop")
                .status(ShopStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        ShopResponse response = mapper.toShopResponse(entity, null);

        // Then
        assertNotNull(response);
        assertEquals(entity.getId(), response.getId());
        assertEquals(entity.getShopName(), response.getShopName());
        assertNull(response.getOwnerName());
        assertNull(response.getOwnerEmail());
    }

    @Test
    void toShopEntity_FromCreateRequest_ShouldMapCorrectly() {
        // Given
        CreateShopRequest request = CreateShopRequest.builder()
                .shopName("New Shop")
                .slug("new-shop")
                .description("New shop description")
                .email("newshop@example.com")
                .phone("0987654321")
                .province("HCMC")
                .district("District 1")
                .addressDetail("456 New Street")
                .build();

        // When
        ShopEntity entity = mapper.toShopEntity(request);

        // Then
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getOwnerId());
        assertEquals(request.getShopName(), entity.getShopName());
        assertEquals(request.getSlug(), entity.getSlug());
        assertEquals(request.getDescription(), entity.getDescription());
        assertEquals(request.getEmail(), entity.getEmail());
        assertEquals(request.getPhone(), entity.getPhone());
        assertEquals(request.getProvince(), entity.getProvince());
        assertEquals(request.getDistrict(), entity.getDistrict());
        assertEquals(request.getAddressDetail(), entity.getAddressDetail());
    }

    @Test
    void updateShopEntity_FromUpdateRequest_ShouldUpdateFields() {
        // Given
        ShopEntity existingEntity = ShopEntity.builder()
                .id(1L)
                .ownerId(1L)
                .shopName("Old Shop")
                .slug("old-shop")
                .description("Old description")
                .email("old@example.com")
                .phone("0111111111")
                .status(ShopStatus.ACTIVE)
                .build();

        UpdateShopRequest request = UpdateShopRequest.builder()
                .shopName("Updated Shop")
                .description("Updated description")
                .email("updated@example.com")
                .phone("0222222222")
                .province("Hanoi")
                .district("Ba Dinh")
                .addressDetail("789 Updated Street")
                .build();

        // When
        mapper.updateShopEntity(request, existingEntity);

        // Then
        assertEquals(1L, existingEntity.getId()); // Should not change
        assertEquals(1L, existingEntity.getOwnerId()); // Should not change
        assertEquals("old-shop", existingEntity.getSlug()); // Should not change
        assertEquals(ShopStatus.ACTIVE, existingEntity.getStatus()); // Should not change
        assertEquals(request.getShopName(), existingEntity.getShopName());
        assertEquals(request.getDescription(), existingEntity.getDescription());
        assertEquals(request.getEmail(), existingEntity.getEmail());
        assertEquals(request.getPhone(), existingEntity.getPhone());
        assertEquals(request.getProvince(), existingEntity.getProvince());
        assertEquals(request.getDistrict(), existingEntity.getDistrict());
        assertEquals(request.getAddressDetail(), existingEntity.getAddressDetail());
    }
}
