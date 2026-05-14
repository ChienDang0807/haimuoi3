package vn.chiendt.haimuoi3.sysadmin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.shop.model.postgres.ShopEntity;
import vn.chiendt.haimuoi3.shop.model.postgres.ShopStatus;
import vn.chiendt.haimuoi3.shop.repository.ShopRepository;
import vn.chiendt.haimuoi3.sysadmin.dto.request.AssignOwnerRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.response.ShopListResponse;
import vn.chiendt.haimuoi3.sysadmin.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.sysadmin.mapper.SysadminShopMapper;
import vn.chiendt.haimuoi3.sysadmin.service.ShopManagementService;
import vn.chiendt.haimuoi3.sysadmin.validator.SysadminShopValidator;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.model.postgres.UserRole;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopManagementServiceImpl implements ShopManagementService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SysadminShopMapper shopMapper;
    private final SysadminShopValidator shopValidator;

    @Override
    @Transactional
    public ShopResponse createShop(CreateShopRequest request) {
        shopValidator.validateCreateShopRequest(request);

        // Check if slug already exists
        if (shopRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new IllegalArgumentException("Shop with slug '" + request.getSlug() + "' already exists");
        }

        ShopEntity shop = shopMapper.toShopEntity(request);
        shop.setStatus(ShopStatus.ACTIVE);

        ShopEntity savedShop = shopRepository.save(shop);
        return shopMapper.toShopResponse(savedShop, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopListResponse findAllShops(Pageable pageable) {
        Page<ShopEntity> shopPage = shopRepository.findAll(pageable);

        List<ShopResponse> shops = shopPage.getContent().stream()
                .map(shop -> {
                    UserEntity owner = shop.getOwnerId() != null
                            ? userRepository.findById(shop.getOwnerId()).orElse(null)
                            : null;
                    return shopMapper.toShopResponse(shop, owner);
                })
                .collect(Collectors.toList());

        return ShopListResponse.builder()
                .shops(shops)
                .currentPage(shopPage.getNumber())
                .totalPages(shopPage.getTotalPages())
                .totalElements(shopPage.getTotalElements())
                .pageSize(shopPage.getSize())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getShopById(Long shopId) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

        UserEntity owner = shop.getOwnerId() != null
                ? userRepository.findById(shop.getOwnerId()).orElse(null)
                : null;

        return shopMapper.toShopResponse(shop, owner);
    }

    @Override
    @Transactional
    public ShopResponse updateShop(Long shopId, UpdateShopRequest request) {
        shopValidator.validateUpdateShopRequest(request);

        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

        shopMapper.updateShopEntity(request, shop);

        ShopEntity updatedShop = shopRepository.save(shop);

        UserEntity owner = updatedShop.getOwnerId() != null
                ? userRepository.findById(updatedShop.getOwnerId()).orElse(null)
                : null;

        return shopMapper.toShopResponse(updatedShop, owner);
    }

    @Override
    @Transactional
    public void deleteShop(Long shopId) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

        // Check if shop has products
        boolean hasProducts = !productRepository.findByShopId(String.valueOf(shopId), Pageable.unpaged()).isEmpty();
        if (hasProducts) {
            throw new IllegalStateException("Cannot delete shop with existing products");
        }

        shopRepository.delete(shop);
    }

    @Override
    @Transactional
    public ShopResponse assignOwner(Long shopId, AssignOwnerRequest request) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

        if (shop.getOwnerId() != null) {
            throw new IllegalStateException("Shop already has an owner. Use changeOwner to update.");
        }

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (user.getRole() != UserRole.SHOP_OWNER) {
            throw new IllegalArgumentException("User must have SHOP_OWNER role to be assigned as shop owner");
        }

        // Check if user already owns a shop
        if (shopRepository.findByOwnerId(request.getUserId()).isPresent()) {
            throw new IllegalStateException("User already owns a shop");
        }

        shop.setOwnerId(request.getUserId());
        ShopEntity updatedShop = shopRepository.save(shop);

        return shopMapper.toShopResponse(updatedShop, user);
    }

    @Override
    @Transactional
    public ShopResponse changeOwner(Long shopId, AssignOwnerRequest request) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

        UserEntity newOwner = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (newOwner.getRole() != UserRole.SHOP_OWNER) {
            throw new IllegalArgumentException("User must have SHOP_OWNER role to be assigned as shop owner");
        }

        // Check if new owner already owns another shop
        shopRepository.findByOwnerId(request.getUserId()).ifPresent(existingShop -> {
            if (!existingShop.getId().equals(shopId)) {
                throw new IllegalStateException("User already owns another shop");
            }
        });

        shop.setOwnerId(request.getUserId());
        ShopEntity updatedShop = shopRepository.save(shop);

        return shopMapper.toShopResponse(updatedShop, newOwner);
    }

    @Override
    @Transactional
    public ShopResponse removeOwner(Long shopId) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

        if (shop.getOwnerId() == null) {
            throw new IllegalStateException("Shop does not have an owner");
        }

        shop.setOwnerId(null);
        ShopEntity updatedShop = shopRepository.save(shop);

        return shopMapper.toShopResponse(updatedShop, null);
    }
}
