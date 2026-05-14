package vn.chiendt.haimuoi3.user.mapper;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.user.dto.response.AddressResponse;
import vn.chiendt.haimuoi3.user.model.postgres.AddressEntity;

@Component
public class AddressMapper {

    public AddressResponse toResponse(AddressEntity entity) {
        return AddressResponse.builder()
                .id(entity.getId())
                .addressName(entity.getAddressName())
                .recipientName(entity.getRecipientName())
                .phone(entity.getPhone())
                .province(entity.getProvince())
                .district(entity.getDistrict())
                .ward(entity.getWard())
                .streetAddress(entity.getStreetAddress())
                .isDefault(entity.getIsDefault())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
