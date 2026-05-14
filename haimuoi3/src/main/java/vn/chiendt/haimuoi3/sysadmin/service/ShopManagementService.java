package vn.chiendt.haimuoi3.sysadmin.service;

import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.sysadmin.dto.request.AssignOwnerRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.response.ShopListResponse;
import vn.chiendt.haimuoi3.sysadmin.dto.response.ShopResponse;

public interface ShopManagementService {

    /**
     * Create a new shop
     */
    ShopResponse createShop(CreateShopRequest request);

    /**
     * Get all shops with pagination
     */
    ShopListResponse findAllShops(Pageable pageable);

    /**
     * Get shop by ID
     */
    ShopResponse getShopById(Long shopId);

    /**
     * Update shop information
     */
    ShopResponse updateShop(Long shopId, UpdateShopRequest request);

    /**
     * Delete shop (only if no products exist)
     */
    void deleteShop(Long shopId);

    /**
     * Assign owner to shop
     */
    ShopResponse assignOwner(Long shopId, AssignOwnerRequest request);

    /**
     * Change shop owner
     */
    ShopResponse changeOwner(Long shopId, AssignOwnerRequest request);

    /**
     * Remove owner from shop
     */
    ShopResponse removeOwner(Long shopId);
}
