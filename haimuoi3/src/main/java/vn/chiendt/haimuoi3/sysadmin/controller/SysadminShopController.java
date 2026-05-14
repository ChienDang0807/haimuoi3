package vn.chiendt.haimuoi3.sysadmin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.sysadmin.dto.request.AssignOwnerRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.response.ShopListResponse;
import vn.chiendt.haimuoi3.sysadmin.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.sysadmin.service.ShopManagementService;

@RestController
@RequestMapping("/api/v1/sysadmin/shops")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SysadminShopController {

    private final ShopManagementService shopManagementService;

    /**
     * GET /api/v1/sysadmin/shops - List all shops with pagination
     */
    @GetMapping
    public ApiResponse<ShopListResponse> getAllShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        ShopListResponse response = shopManagementService.findAllShops(pageable);
        return ApiResponse.success(response, "Shops retrieved successfully");
    }

    /**
     * GET /api/v1/sysadmin/shops/{shopId} - Get shop detail
     */
    @GetMapping("/{shopId}")
    public ApiResponse<ShopResponse> getShopById(@PathVariable Long shopId) {
        ShopResponse response = shopManagementService.getShopById(shopId);
        return ApiResponse.success(response, "Shop retrieved successfully");
    }

    /**
     * POST /api/v1/sysadmin/shops - Create new shop
     */
    @PostMapping
    public ApiResponse<ShopResponse> createShop(@RequestBody CreateShopRequest request) {
        ShopResponse response = shopManagementService.createShop(request);
        return ApiResponse.success(response, "Shop created successfully");
    }

    /**
     * PUT /api/v1/sysadmin/shops/{shopId} - Update shop
     */
    @PutMapping("/{shopId}")
    public ApiResponse<ShopResponse> updateShop(
            @PathVariable Long shopId,
            @RequestBody UpdateShopRequest request) {
        ShopResponse response = shopManagementService.updateShop(shopId, request);
        return ApiResponse.success(response, "Shop updated successfully");
    }

    /**
     * DELETE /api/v1/sysadmin/shops/{shopId} - Delete shop
     */
    @DeleteMapping("/{shopId}")
    public ApiResponse<Void> deleteShop(@PathVariable Long shopId) {
        shopManagementService.deleteShop(shopId);
        return ApiResponse.success(null, "Shop deleted successfully");
    }

    /**
     * POST /api/v1/sysadmin/shops/{shopId}/owner - Assign owner to shop
     */
    @PostMapping("/{shopId}/owner")
    public ApiResponse<ShopResponse> assignOwner(
            @PathVariable Long shopId,
            @RequestBody AssignOwnerRequest request) {
        ShopResponse response = shopManagementService.assignOwner(shopId, request);
        return ApiResponse.success(response, "Owner assigned successfully");
    }

    /**
     * PUT /api/v1/sysadmin/shops/{shopId}/owner - Change shop owner
     */
    @PutMapping("/{shopId}/owner")
    public ApiResponse<ShopResponse> changeOwner(
            @PathVariable Long shopId,
            @RequestBody AssignOwnerRequest request) {
        ShopResponse response = shopManagementService.changeOwner(shopId, request);
        return ApiResponse.success(response, "Owner changed successfully");
    }

    /**
     * DELETE /api/v1/sysadmin/shops/{shopId}/owner - Remove owner from shop
     */
    @DeleteMapping("/{shopId}/owner")
    public ApiResponse<ShopResponse> removeOwner(@PathVariable Long shopId) {
        ShopResponse response = shopManagementService.removeOwner(shopId);
        return ApiResponse.success(response, "Owner removed successfully");
    }
}
