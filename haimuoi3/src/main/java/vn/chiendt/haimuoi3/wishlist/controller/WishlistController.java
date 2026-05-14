package vn.chiendt.haimuoi3.wishlist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.wishlist.dto.request.WishlistContainsRequest;
import vn.chiendt.haimuoi3.wishlist.dto.response.WishlistContainsResponse;
import vn.chiendt.haimuoi3.wishlist.dto.response.WishlistItemResponse;
import vn.chiendt.haimuoi3.wishlist.service.WishlistService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers/me/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/recent")
    public ApiResponse<List<WishlistItemResponse>> listRecent(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestParam(name = "limit", defaultValue = "3") int limit) {
        List<WishlistItemResponse> list = wishlistService.listRecent(currentUser.getId(), limit);
        return ApiResponse.success(list, "Wishlist recent items retrieved successfully");
    }

    @GetMapping
    public ApiResponse<Page<WishlistItemResponse>> listPaged(
            @AuthenticationPrincipal UserEntity currentUser,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<WishlistItemResponse> page = wishlistService.listPaged(currentUser.getId(), pageable);
        return ApiResponse.success(page, "Wishlist retrieved successfully");
    }

    @PostMapping("/contains")
    public ApiResponse<WishlistContainsResponse> contains(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestBody WishlistContainsRequest request) {
        WishlistContainsResponse response = wishlistService.contains(currentUser.getId(), request);
        return ApiResponse.success(response, "Wishlist contains resolved successfully");
    }

    @PostMapping("/{productId}")
    public ApiResponse<WishlistItemResponse> add(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable String productId) {
        WishlistItemResponse response = wishlistService.add(currentUser.getId(), productId);
        return ApiResponse.success(response, "Product added to wishlist successfully");
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> remove(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable String productId) {
        wishlistService.remove(currentUser.getId(), productId);
        return ApiResponse.success(null, "Product removed from wishlist successfully");
    }
}
