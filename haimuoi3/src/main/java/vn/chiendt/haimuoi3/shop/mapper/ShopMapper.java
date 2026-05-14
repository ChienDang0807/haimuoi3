package vn.chiendt.haimuoi3.shop.mapper;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.shop.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.shop.model.postgres.ShopEntity;

@Component
public class ShopMapper {

    public ShopResponse toResponse(ShopEntity entity) {
        return ShopResponse.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .shopName(entity.getShopName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .logoUrl(entity.getLogoUrl())
                .bannerUrl(entity.getBannerUrl())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .province(entity.getProvince())
                .district(entity.getDistrict())
                .addressDetail(entity.getAddressDetail())
                .status(entity.getStatus().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
