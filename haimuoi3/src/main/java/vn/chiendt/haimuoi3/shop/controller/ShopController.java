package vn.chiendt.haimuoi3.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.order.dto.request.UpdateOrderStatusRequest;
import vn.chiendt.haimuoi3.order.dto.response.OrderResponse;
import vn.chiendt.haimuoi3.order.service.OrderService;
import vn.chiendt.haimuoi3.product.dto.request.CreateProductRequest;
import vn.chiendt.haimuoi3.product.dto.response.ShopProductResponse;
import vn.chiendt.haimuoi3.product.service.ProductService;
import vn.chiendt.haimuoi3.shop.dto.request.CreateShopCategoryRequest;
import vn.chiendt.haimuoi3.shop.dto.request.UpdateShopRequest;
import vn.chiendt.haimuoi3.shop.dto.response.ShopCategoryResponse;
import vn.chiendt.haimuoi3.shop.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.shop.service.ShopCategoryService;
import vn.chiendt.haimuoi3.shop.service.ShopService;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final OrderService orderService;
    private final ProductService productService;
    private final ShopCategoryService shopCategoryService;

    @GetMapping("/my-shop")
    public ApiResponse<ShopResponse> getMyShop(@AuthenticationPrincipal UserEntity currentUser) {
        ShopResponse shop = shopService.getShopByOwnerId(currentUser.getId());
        return ApiResponse.success(shop, "Shop retrieved successfully");
    }

    @PutMapping("/my-shop")
    public ApiResponse<ShopResponse> updateMyShop(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestBody UpdateShopRequest request) {
        ShopResponse shop = shopService.updateShop(currentUser.getId(), request);
        return ApiResponse.success(shop, "Shop updated successfully");
    }

    @GetMapping("/my-shop/orders")
    public ApiResponse<Page<OrderResponse>> listMyShopOrders(
            @AuthenticationPrincipal UserEntity currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        ShopResponse shop = shopService.getShopByOwnerId(currentUser.getId());
        Page<OrderResponse> page = orderService.getOrdersByShopId(shop.getId(), pageable);
        return ApiResponse.success(page, "Orders retrieved successfully");
    }

    @PatchMapping("/my-shop/orders/{id}/status")
    public ApiResponse<OrderResponse> updateMyShopOrderStatus(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse updated = orderService.updateOrderStatusByShopOwner(currentUser.getId(), id, request);
        return ApiResponse.success(updated, "Order status updated successfully");
    }

    @GetMapping("/my-shop/products")
    public ApiResponse<Page<ShopProductResponse>> listMyShopProducts(
            @AuthenticationPrincipal UserEntity currentUser,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        ShopResponse shop = shopService.getShopByOwnerId(currentUser.getId());
        Page<ShopProductResponse> page = productService.findAllByShopId(String.valueOf(shop.getId()), pageable);
        return ApiResponse.success(page, "Shop products retrieved successfully");
    }

    @PostMapping("/my-shop/products")
    public ApiResponse<ShopProductResponse> createMyShopProduct(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestBody CreateProductRequest request) {
        ShopResponse shop = shopService.getShopByOwnerId(currentUser.getId());
        request.setShopId(String.valueOf(shop.getId()));
        ShopProductResponse created = productService.save(request);
        return ApiResponse.success(created, "Product created successfully");
    }

    @GetMapping("/my-shop/categories")
    public ApiResponse<Page<ShopCategoryResponse>> listMyShopCategories(
            @AuthenticationPrincipal UserEntity currentUser,
            @PageableDefault(size = 50, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ShopCategoryResponse> page = shopCategoryService.listForShopOwner(currentUser.getId(), pageable);
        return ApiResponse.success(page, "Shop categories retrieved successfully");
    }

    @PostMapping("/my-shop/categories")
    public ApiResponse<ShopCategoryResponse> createMyShopCategory(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestBody CreateShopCategoryRequest request) {
        ShopCategoryResponse created = shopCategoryService.createShopCategory(currentUser.getId(), request);
        return ApiResponse.success(created, "Shop category created successfully");
    }

    @GetMapping("/by-id/{id}")
    public ApiResponse<ShopResponse> getShopById(@PathVariable Long id) {
        ShopResponse shop = shopService.getShopById(id);
        return ApiResponse.success(shop, "Shop retrieved successfully");
    }

    @GetMapping("/{slug}")
    public ApiResponse<ShopResponse> getShopBySlug(@PathVariable String slug) {
        ShopResponse shop = shopService.getShopBySlug(slug);
        return ApiResponse.success(shop, "Shop retrieved successfully");
    }
}
