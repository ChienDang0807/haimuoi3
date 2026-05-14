package vn.chiendt.haimuoi3.shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.shop.dto.request.UpdateShopRequest;
import vn.chiendt.haimuoi3.shop.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.shop.mapper.ShopMapper;
import vn.chiendt.haimuoi3.shop.model.postgres.ShopEntity;
import vn.chiendt.haimuoi3.shop.model.postgres.ShopStatus;
import vn.chiendt.haimuoi3.shop.repository.ShopRepository;
import vn.chiendt.haimuoi3.shop.service.ShopService;
import vn.chiendt.haimuoi3.shop.validator.UpdateShopValidator;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UpdateShopValidator updateShopValidator;
    private final ShopMapper shopMapper;

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getShopByOwnerId(Long ownerId) {
        ShopEntity shop = shopRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for this owner"));

        return shopMapper.toResponse(shop);
    }

    @Override
    @Transactional
    public ShopResponse updateShop(Long ownerId, UpdateShopRequest request) {
        updateShopValidator.validate(request);

        ShopEntity shop = shopRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for this owner"));

        if (request.getShopName() != null && !request.getShopName().isBlank()) {
            shop.setShopName(request.getShopName());
        }
        if (request.getDescription() != null) {
            shop.setDescription(request.getDescription());
        }
        if (request.getLogoUrl() != null) {
            shop.setLogoUrl(request.getLogoUrl());
        }
        if (request.getBannerUrl() != null) {
            shop.setBannerUrl(request.getBannerUrl());
        }
        if (request.getEmail() != null) {
            shop.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            shop.setPhone(request.getPhone());
        }
        if (request.getProvince() != null) {
            shop.setProvince(request.getProvince());
        }
        if (request.getDistrict() != null) {
            shop.setDistrict(request.getDistrict());
        }
        if (request.getAddressDetail() != null) {
            shop.setAddressDetail(request.getAddressDetail());
        }

        ShopEntity updatedShop = shopRepository.save(shop);
        return shopMapper.toResponse(updatedShop);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getShopBySlug(String slug) {
        ShopEntity shop = shopRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new ResourceNotFoundException("Shop is not active");
        }

        return shopMapper.toResponse(shop);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getShopById(Long id) {
        ShopEntity shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new ResourceNotFoundException("Shop is not active");
        }

        return shopMapper.toResponse(shop);
    }
}
